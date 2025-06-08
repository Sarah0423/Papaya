package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeliveryStatus extends AppCompatActivity {

    private Spinner spinnerOrders;
    private TextView etaText, detailText;
    private ProgressBar progressBar;
    private ImageView deliveryIcon;
    private Handler handler = new Handler();
    private List<OrderItem> deliveryOrders = new ArrayList<>();
    private int currentIndex = 0;
    private Runnable updateRunnable;
    private FirebaseFirestore db;
    private String uid;
    private ImageButton btnReturn;

    private static final long TOTAL_TIME = 5 * 60 * 1000; // 5 分鐘
    private static final long INTERVAL = 1000; // 每秒更新一次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        spinnerOrders = findViewById(R.id.spinner_orders);
        etaText = findViewById(R.id.etaText);
        detailText = findViewById(R.id.tv_order_detail_text);
        progressBar = findViewById(R.id.progressBar);
        deliveryIcon = findViewById(R.id.deliveryIcon);
        btnReturn = findViewById(R.id.btn_return_to_order);

        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(DeliveryStatus.this, MainActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadOrders();
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("order_user_id", uid)
                .get()
                .addOnSuccessListener(query -> {
                    deliveryOrders.clear();
                    List<String> spinnerItems = new ArrayList<>();
                    long now = System.currentTimeMillis();

                    for (QueryDocumentSnapshot doc : query) {
                        Timestamp orderTime = doc.getTimestamp("order_time");
                        if (orderTime == null) continue;

                        long orderMillis = orderTime.toDate().getTime();
                        long diffMillis = now - orderMillis;

                        if (diffMillis > 10 * 60 * 1000) continue;

                        Map<String, Object> deliveryInfo = (Map<String, Object>) doc.get("delivery_info");
                        String address = deliveryInfo != null ? (String) deliveryInfo.get("address") : "";
                        if (address != null && address.contains("自取")) continue;

                        String orderId = doc.getId();

                        db.collection("orders")
                                .document(orderId)
                                .collection("summary")
                                .get()
                                .addOnSuccessListener(summaryQuery -> {
                                    long price = 0;
                                    for (QueryDocumentSnapshot summaryDoc : summaryQuery) {
                                        Long p = summaryDoc.getLong("total_price");
                                        if (p != null) price = p;
                                        break;
                                    }

                                    OrderItem order = new OrderItem(orderId, "", price, address, orderMillis);
                                    deliveryOrders.add(order);
                                    spinnerItems.add("訂單：" + orderId.substring(0, Math.min(12, orderId.length())) + "...");

                                    // 只有在第一筆加入後才初始化 UI（防止 spinner 重複設定）
                                    if (deliveryOrders.size() == 1) {
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                                android.R.layout.simple_spinner_dropdown_item, spinnerItems);
                                        spinnerOrders.setAdapter(adapter);

                                        spinnerOrders.setVisibility(View.VISIBLE);
                                        spinnerOrders.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                                currentIndex = position;
                                                updateOrderUI();
                                            }

                                            @Override
                                            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                                        });

                                        updateOrderUI();
                                    } else {
                                        // 多筆時只更新下拉內容
                                        ((ArrayAdapter) spinnerOrders.getAdapter()).notifyDataSetChanged();
                                    }
                                });
                    }

                    if (deliveryOrders.isEmpty()) {
                        etaText.setText("目前沒有進行中的外送");
                        detailText.setText("");
                        progressBar.setProgress(0);
                        spinnerOrders.setVisibility(View.GONE);
                    }
                });
    }

    private void updateOrderUI() {
        if (deliveryOrders.isEmpty()) return;

        OrderItem item = deliveryOrders.get(currentIndex);
        long now = System.currentTimeMillis();
        long elapsed = now - item.getOrderTimeMillis();
        long remaining = Math.max(0, TOTAL_TIME - elapsed);
        long totalElapsed = elapsed;

        int remainingMinutes = (int) (remaining / 60000);
        int remainingSeconds = (int) ((remaining / 1000) % 60);
        int progress = (int) ((remaining / (float) TOTAL_TIME) * 100);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        String formattedTime = formatter.format(new Date(item.getOrderTimeMillis()));

        if (totalElapsed >= 5 * 60 * 1000) {
            etaText.setText("外送已送達！");
            deliveryIcon.setImageResource(R.mipmap.deliver_arrive);
            progressBar.setProgress(0);
        } else {
            etaText.setText("預計送達時間：" + remainingMinutes + " 分 " + remainingSeconds + " 秒");
            deliveryIcon.setImageResource(R.mipmap.ic_delivery_bike);
            progressBar.setProgress(progress);
        }

        detailText.setText(
                "訂單編號：" + item.getOrderId().substring(0, Math.min(12, item.getOrderId().length())) + "...\n" +
                        "地址：" + item.getAddress() + "\n" +
                        "金額：$" + item.getFinalPrice() + "\n" +
                        "訂購時間：" + formattedTime
        );

        if (updateRunnable != null) handler.removeCallbacks(updateRunnable);
        if (elapsed > 10 * 60 * 1000) {
            loadOrders();
            return;
        }
        updateRunnable = this::updateOrderUI;
        handler.postDelayed(updateRunnable, INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateRunnable != null) handler.removeCallbacks(updateRunnable);
    }
}
