// OrderInfoActivity.java
package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class OrderInfoActivity extends AppCompatActivity {

    private ImageButton btnReturn;
    private RecyclerView rvOrders;
    private FirebaseFirestore db;
    private String uid;
    private Map<String, String> cityImageMap = new HashMap<>();
    private OrderAdapter adapter;
    private List<OrderItem> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn = findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(OrderInfoActivity.this, MainActivity.class);
            intent.putExtra("SHOW_FUNCTION_MENU", true);
            startActivity(intent);
            finish();
        });

        rvOrders = findViewById(R.id.rv_item_order);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, orderList, item -> {
            showOrderDetailDialog(item.getOrderId());
        });

        rvOrders.setAdapter(adapter);

        int spaceInDp = 16;
        float density = getResources().getDisplayMetrics().density;
        int spaceInPx = Math.round(spaceInDp * density);
        rvOrders.addItemDecoration(new VerticalSpaceItemDecoration(spaceInPx));


        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadMapImages();
    }

    private void loadMapImages() {
        db.collection("map").get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query) {
                String city = doc.getString("map_city");
                String imgName = doc.getString("map_img");
                if (city != null && imgName != null) {
                    cityImageMap.put(city, imgName);
                }
            }
            loadOrders();
        });
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("order_user_id", uid)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot orderDoc : query) {
                        String orderId = orderDoc.getId();
                        Map<String, Object> deliveryInfo = (Map<String, Object>) orderDoc.get("delivery_info");
                        String branch = deliveryInfo != null ? (String) deliveryInfo.get("branch") : "";
                        String cityKey = branch.length() >= 2 ? branch.substring(0, 2) : branch;
                        String mapImg = cityImageMap.getOrDefault(cityKey, "default_map");

                        db.collection("orders")
                                .document(orderId)
                                .collection("summary")
                                .get()
                                .addOnSuccessListener(summaryDocs -> {
                                    long finalTotal = 0;
                                    for (DocumentSnapshot summary : summaryDocs) {
                                        Long price = summary.getLong("total_price");
                                        Long shipping = summary.getLong("shipping_fee");
                                        Long discount = summary.getLong("discount");
                                        finalTotal = (price != null ? price : 0) + (shipping != null ? shipping : 0) - (discount != null ? discount : 0);
                                        break; // ✅ 只取第一筆 summary 文件
                                    }

                                    String address = deliveryInfo != null ? (String) deliveryInfo.get("address") : "";
                                    long orderTimeMillis = orderDoc.getTimestamp("order_time") != null
                                            ? orderDoc.getTimestamp("order_time").toDate().getTime()
                                            : 0;

                                    OrderItem data = new OrderItem(orderId, mapImg, finalTotal, address, orderTimeMillis);

                                    orderList.add(data);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private void showOrderDetailDialog(String orderId) {
        db.collection("orders").document(orderId).get().addOnSuccessListener(orderDoc -> {
            if (!orderDoc.exists()) return;

            Map<String, Object> deliveryInfo = (Map<String, Object>) orderDoc.get("delivery_info");
            String name = deliveryInfo != null ? (String) deliveryInfo.get("name") : "";
            String phone = deliveryInfo != null ? (String) deliveryInfo.get("phone") : "";
            String address = deliveryInfo != null ? (String) deliveryInfo.get("address") : "";
            String branch = deliveryInfo != null ? (String) deliveryInfo.get("branch") : "";
            String cardNum = orderDoc.getString("order_card_number");
            String cardDisplay = cardNum != null && cardNum.length() >= 16
                    ? "**** **** **** " + cardNum.substring(12)
                    : "**** **** **** ????";
            final String[] time = {""};

            Timestamp timestamp = orderDoc.getTimestamp("order_time");
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Taipei"));
                time[0] = sdf.format(date);
            }

            db.collection("orders").document(orderId).collection("summary")
                    .get().addOnSuccessListener(summaryDocs -> {
                        final long[] finalPrice = {0};
                        for (DocumentSnapshot summary : summaryDocs) {
                            Long p = summary.getLong("total_price");
                            Long s = summary.getLong("shipping_fee");
                            Long d = summary.getLong("discount");
                            finalPrice[0] = (p != null ? p : 0) + (s != null ? s : 0) - (d != null ? d : 0);
                            break;
                        }

                        db.collection("orders").document(orderId).collection("item")
                                .get().addOnSuccessListener(itemDocs -> {
                                    StringBuilder itemInfo = new StringBuilder();
                                    for (DocumentSnapshot item : itemDocs) {
                                        String itemName = item.getString("item_name");
                                        String itemSelected = item.getString("item_selected");
                                        Long quantity = item.getLong("item_quantity");
                                        itemInfo.append("- ")
                                                .append(itemName != null ? itemName : "")
                                                .append("（")
                                                .append(itemSelected != null ? itemSelected : "無配料")
                                                .append("）x")
                                                .append(quantity != null ? quantity : 1)
                                                .append("\n");
                                    }

                                    // 顯示 Dialog
                                    String message =
                                            "訂單編號：" + orderId + "\n\n" +
                                                    "姓名：" + name + "\n" +
                                                    "電話：" + phone + "\n" +
                                                    "地址：" + address + "\n" +
                                                    "訂購分店：" + branch + "\n\n" +
                                                    "信用卡號：" + cardDisplay + "\n" +
                                                    "訂購時間：" + time[0] + "\n\n" +
                                                    "訂購商品：\n" + itemInfo + "\n" +
                                                    "=============================\n" +
                                                    "訂單金額：$" + finalPrice[0] + "\n";

                                    new androidx.appcompat.app.AlertDialog.Builder(OrderInfoActivity.this)
                                            .setTitle("訂單詳細資訊")
                                            .setMessage(message)
                                            .setPositiveButton("確定", null)
                                            .show();
                                });
                    });
        });
    }

}
