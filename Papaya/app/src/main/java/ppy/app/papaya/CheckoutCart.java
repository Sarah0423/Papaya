package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutCart extends AppCompatActivity {

    private String userId;
    private ImageButton btnReturn;
    private Button btnDeliveryInfo;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uid;
    private TextView total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button btnChooseCoupon = findViewById(R.id.btn_choose_coupon);
        btnChooseCoupon.setOnClickListener(v -> showAvailableCoupons());

        btnReturn = findViewById(R.id.btn_return_to_cart);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, CheckCartActivity.class);
            startActivity(intent);
            finish();
        });

        btnDeliveryInfo = findViewById(R.id.btn_write_delivery_info);
        btnDeliveryInfo.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, DeliveryInfo.class);
            startActivity(intent);
            finish();
        });

        total = findViewById(R.id.rginpok5lmng);
        long totalAmount = getIntent().getLongExtra("totalAmount", 0);
        total.setText("$" + totalAmount);

        TextView tvTotalAmount = findViewById(R.id.rginpok5lmng);
        TextView tvShipping = findViewById(R.id.rugqtneve0y);
        TextView tvDiscount = findViewById(R.id.tv_discount);
        TextView tvFinalTotal = findViewById(R.id.revrvnohagb9);
        TextView tvTotal = findViewById(R.id.tv_total);

        // 從 Firestore 讀取運費與折扣，再計算總金額
        db.collection("users")
                .document(uid)
                .collection("cart_info")
                .document("summary")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //Long shippingFee = documentSnapshot.getLong("shippingFee");
                        Long discount = documentSnapshot.getLong("discount");

                        //if (shippingFee == null) shippingFee = 0L;
                        if (discount == null) discount = 0L;
                        long shippingFee = 30;
                        tvShipping.setText("$" + shippingFee);
                        tvDiscount.setText("-$" + discount);

                        long finalTotal = totalAmount + shippingFee - discount;
                        long Total = totalAmount + shippingFee - discount;
                        tvFinalTotal.setText("$" + finalTotal);
                        tvTotal.setText("$" + finalTotal);

                    } else {
                        tvShipping.setText("$0");
                        tvDiscount.setText("-$0");
                        tvFinalTotal.setText("$" + totalAmount);
                        tvTotal.setText("$" + totalAmount);

                    }
                });

        EditText etName = findViewById(R.id.et_name);
        EditText etCardNum = findViewById(R.id.et_card_num);
        EditText etDate = findViewById(R.id.et_date);
        EditText etVerificationCode = findViewById(R.id.et_tv_verification_code);
        LinearLayout llCommit = findViewById(R.id.ll_commit);



        llCommit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cardNum = etCardNum.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String verificationCode = etVerificationCode.getText().toString().trim();

            if (name.isEmpty() || cardNum.isEmpty() || date.isEmpty() || verificationCode.isEmpty()) {
                Toast.makeText(CheckoutCart.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cardNum.length() != 16 || !cardNum.matches("\\d{16}")) {
                Toast.makeText(CheckoutCart.this, "卡號必須為16位數字", Toast.LENGTH_SHORT).show();
                return;
            }

            String monthStr = date.substring(0, 2);
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                Toast.makeText(CheckoutCart.this, "月份必須在01到12之間", Toast.LENGTH_SHORT).show();
                return;
            }

            if (verificationCode.length() != 3 || !verificationCode.matches("\\d{3}")) {
                Toast.makeText(CheckoutCart.this, "驗證碼必須為3位數字", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(CheckoutCart.this, "付款成功，謝謝您的訂購！", Toast.LENGTH_LONG).show();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Step 1：建立 order 文件（先建好，再取得 ID）
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order_user_id", uid);
            orderData.put("order_card_number", cardNum);

            DocumentReference deliveryInfoRef = db.collection("users").document(uid).collection("delivery").document("delivery_info");
            deliveryInfoRef.get().addOnSuccessListener(deliverySnapshot -> {
                if (deliverySnapshot.exists()) {
                    Map<String, Object> deliveryInfo = deliverySnapshot.getData();
                    if (deliveryInfo != null) {
                        orderData.put("delivery_info", deliveryInfo);
                    }
                }

                // 建立 order 文件
                db.collection("orders").add(orderData).addOnSuccessListener(orderDocRef -> {
                    // Step 2：取得使用者 cart_item 所有文件
                    db.collection("users").document(uid).collection("cart_item")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    Map<String, Object> itemData = new HashMap<>();
                                    itemData.put("item_name", doc.getString("item_name"));
                                    itemData.put("item_price", doc.getLong("item_price")); // 注意型別
                                    itemData.put("item_quantity", doc.getLong("item_quantity"));
                                    itemData.put("item_selected", doc.getString("item_selected"));

                                    // 加入到 order 的 item 子集合，ID 同原本 cart_item 的文件 ID
                                    orderDocRef.collection("item").document(doc.getId())
                                            .set(itemData)
                                            .addOnSuccessListener(aVoid -> {
                                                // 資料寫入成功後，刪除原始 cart_item 文件
                                                db.collection("users").document(uid)
                                                        .collection("cart_item").document(doc.getId())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            // 刪除成功，可加上 Log 或提示
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // 刪除失敗，可加上錯誤處理
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                // 寫入 order/item 失敗處理
                                            });
                                }

                                // 最後跳轉頁面
                                Intent intent = new Intent(CheckoutCart.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                });
            });
        });

    }

    private void showAvailableCoupons() {
        // 取得當前時間
        Date currentDate = new Date();

        // 從 Firestore 讀取用戶的 owned_coupons
        db.collection("users")
                .document(userId)
                .collection("owned_coupons")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> availableCoupons = new ArrayList<>();
                        List<String> couponIdsToDelete = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            boolean isUsed = document.getBoolean("is_used");
                            Date expireAt = document.getDate("expire_at");
                            int couponIndex = document.getLong("coupon_index").intValue();

                            // 判斷優惠券是否過期或已使用
                            if (isUsed || expireAt == null || expireAt.before(currentDate)) {
                                couponIdsToDelete.add(document.getId());
                            } else {
                                // 根據 coupon_index 查詢主集合中的 coupons
                                db.collection("coupons")
                                        .whereEqualTo("coupon_index", couponIndex)
                                        .get()
                                        .addOnCompleteListener(couponTask -> {
                                            if (couponTask.isSuccessful()) {
                                                for (QueryDocumentSnapshot couponDoc : couponTask.getResult()) {
                                                    String couponName = couponDoc.getString("coupon_name");
                                                    Date couponExpireAt = couponDoc.getDate("expire_at");

                                                    if (couponName != null && couponExpireAt != null) {
                                                        availableCoupons.add(couponName + " - 到期時間: " + couponExpireAt.toString());
                                                    }
                                                }

                                                // 刪除過期或已使用的優惠券
                                                deleteExpiredOrUsedCoupons(couponIdsToDelete);

                                                // 顯示可用的優惠券
                                                if (!availableCoupons.isEmpty()) {
                                                    runOnUiThread(() -> showCouponSelectionDialog(availableCoupons));
                                                } else {
                                                    runOnUiThread(() -> Toast.makeText(this, "目前沒有可用的優惠券", Toast.LENGTH_SHORT).show());
                                                }
                                            } else {
                                                Log.e("CheckoutCart", "Error getting coupons: ", couponTask.getException());
                                            }
                                        });
                            }
                        }
                    } else {
                        Log.e("CheckoutCart", "Error getting owned_coupons: ", task.getException());
                    }
                });
    }



    private void deleteExpiredOrUsedCoupons(List<String> couponIdsToDelete) {
        for (String couponId : couponIdsToDelete) {
            db.collection("users")
                    .document(userId)
                    .collection("owned_coupons")
                    .document(couponId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("CheckoutCart", "Coupon deleted successfully"))
                    .addOnFailureListener(e -> Log.e("CheckoutCart", "Error deleting coupon: ", e));
        }
    }

    private void showCouponSelectionDialog(List<String> availableCoupons) {
        new AlertDialog.Builder(this)
                .setTitle("選擇優惠券")
                .setItems(availableCoupons.toArray(new String[0]), (dialog, which) -> {
                    String selectedCoupon = availableCoupons.get(which);
                    applyCoupon(selectedCoupon);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void applyCoupon(String selectedCoupon) {
        // 在此處理應用優惠券的邏輯
        // 例如，更新 Firestore 中的訂單資料，或在結帳時應用折扣

        Toast.makeText(this, "已選擇優惠券: " + selectedCoupon, Toast.LENGTH_SHORT).show();
    }
}