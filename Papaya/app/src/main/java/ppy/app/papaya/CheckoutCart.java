package ppy.app.papaya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutCart extends AppCompatActivity {

    private String userId;
    private ImageView ivUser;
    private ImageButton btnReturn;
    private Button btnDeliveryInfo;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uid;
    private TextView total, tvTotalAmount, tvShipping, tvDiscount, tvFinalTotal, tvTotal;

    private EditText etName, etCardNum, etDate, etVerificationCode;

    @Override
    protected void onResume() {
        super.onResume();
        fetchSummaryFromFirestore(); // ✨ 每次回來都重新抓
    }
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


        ivUser = findViewById(R.id.iv_user);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvShipping = findViewById(R.id.tv_shipping);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalTotal = findViewById(R.id.tv_final_total);
        tvTotal = findViewById(R.id.tv_total);

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarName = documentSnapshot.getString("avatar");

                        if (avatarName != null && !avatarName.isEmpty()) {
                            int resId = getResources().getIdentifier(avatarName, "mipmap", getPackageName());
                            if (resId != 0) {
                                ivUser.setImageResource(resId);
                            } else {
                                ivUser.setImageResource(R.mipmap.login_usagi); // 預設圖
                            }
                        } else {
                            ivUser.setImageResource(R.mipmap.login_usagi);
                        }
                    } else {
                        ivUser.setImageResource(R.mipmap.login_usagi);
                    }
                })
                .addOnFailureListener(e -> {
                    ivUser.setImageResource(R.mipmap.login_usagi);
                });




        etName = findViewById(R.id.et_name);
        etCardNum = findViewById(R.id.et_card_num);
        etDate = findViewById(R.id.et_date);
        etVerificationCode = findViewById(R.id.et_tv_verification_code);
        LinearLayout llCommit = findViewById(R.id.ll_commit);

        SharedPreferences prefs = getSharedPreferences("checkout_data", MODE_PRIVATE);
        etName.setText(prefs.getString("name", ""));
        etCardNum.setText(prefs.getString("cardNum", ""));
        etDate.setText(prefs.getString("date", ""));
        etVerificationCode.setText(prefs.getString("code", ""));

        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                saveToPrefs();
            }
        };
        etName.addTextChangedListener(watcher);
        etCardNum.addTextChangedListener(watcher);
        etDate.addTextChangedListener(watcher);
        etVerificationCode.addTextChangedListener(watcher);

        btnReturn = findViewById(R.id.btn_return_to_cart);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, CheckCartActivity.class);
            putEditTextValues(intent);
            startActivity(intent);
        });

        btnChooseCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, ChooseCoupon.class);
            putEditTextValues(intent);
            startActivity(intent);
        });

        btnDeliveryInfo = findViewById(R.id.btn_write_delivery_info);
        btnDeliveryInfo.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, DeliveryInfo.class);
            putEditTextValues(intent);
            startActivity(intent);
        });

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
            SharedPreferences.Editor editor = getSharedPreferences("checkout_data", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

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

                                // 新增 alarm 記錄
                                Map<String, Object> alarmData = new HashMap<>();
                                alarmData.put("alarms_info", "我們已經收到您的訂單，謝謝惠顧！");
                                alarmData.put("alarms_photo", "egg_checkout");
                                alarmData.put("alarms_time", new Date());
                                alarmData.put("alarms_type", "交易完成");

                                db.collection("users")
                                        .document(uid)
                                        .collection("alarms")
                                        .add(alarmData)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d("CheckoutCart", "Alarm added with ID: " + documentReference.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CheckoutCart", "Error adding alarm", e);
                                        });

                                // 最後跳轉頁面
                                Intent intent = new Intent(CheckoutCart.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                });
            });
        });

    }


    private void fetchSummaryFromFirestore() {
        ImageView ivUser = findViewById(R.id.iv_user);
        TextView tvTotalAmount = findViewById(R.id.tv_total_amount);
        TextView tvShipping = findViewById(R.id.tv_shipping);
        TextView tvDiscount = findViewById(R.id.tv_discount);
        TextView tvFinalTotal = findViewById(R.id.tv_final_total);
        TextView tvTotal = findViewById(R.id.tv_total);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("cart_info")
                .document("summary")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long discount = documentSnapshot.getLong("discount");
                        Long totalPrice = documentSnapshot.getLong("total_price");
                        Long shippingFee = documentSnapshot.getLong("shipping_fee");

                        boolean needUpdate = false;
                        if (discount == null) {
                            discount = 0L;
                            needUpdate = true;
                        }
                        if (totalPrice == null) {
                            totalPrice = 0L;
                            needUpdate = true;
                        }
                        if (shippingFee == null) {
                            shippingFee = 30L;
                            needUpdate = true;
                        }

                        if (needUpdate) {
                            Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put("discount", discount);
                            updateMap.put("total_price", totalPrice);
                            updateMap.put("shipping_fee", shippingFee);
                            db.collection("users")
                                    .document(uid)
                                    .collection("cart_info")
                                    .document("summary")
                                    .update(updateMap);
                        }

                        tvShipping.setText("$" + shippingFee);
                        tvDiscount.setText("-$" + discount);
                        tvTotalAmount.setText("$" + totalPrice);
                        long finalTotal = totalPrice + shippingFee - discount;
                        tvFinalTotal.setText("$" + finalTotal);
                        tvTotal.setText("$" + finalTotal);

                    } else {
                        tvShipping.setText("$0");
                        tvDiscount.setText("-$0");
                        tvFinalTotal.setText("$0");
                        tvTotal.setText("$0");
                    }
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
        // 先從 coupon name 取出 coupon 文件（這裡你可以加個 whereEqualTo 查詢）
        db.collection("coupons")
                .whereEqualTo("coupon_name", selectedCoupon.split(" -")[0]) // 去掉到期日後的描述
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot couponDoc = querySnapshot.getDocuments().get(0);
                        String type = couponDoc.getString("coupon_type");
                        long value = couponDoc.getLong("coupon_value");
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        db.collection("users").document(uid).collection("cart_info").document("summary")
                                .get()
                                .addOnSuccessListener(summaryDoc -> {
                                    if (summaryDoc.exists()) {
                                        long totalPrice = summaryDoc.getLong("total_price") != null ? summaryDoc.getLong("total_price") : 0;
                                        long shippingFee = summaryDoc.getLong("shipping_fee") != null ? summaryDoc.getLong("shipping_fee") : 30;
                                        long discount = 0;

                                        switch (type) {
                                            case "discount_percent":
                                                discount = totalPrice * value / 100;
                                                break;
                                            case "discount_amount":
                                                discount = value;
                                                break;
                                            case "free_delivery":
                                                discount = shippingFee;
                                                break;
                                            // 其他 buy_x_get_y 可自行加上
                                        }

                                        long finalTotal = totalPrice + shippingFee - discount;

                                        Map<String, Object> update = new HashMap<>();
                                        update.put("discount", discount);

                                        db.collection("users").document(uid).collection("cart_info").document("summary")
                                                .update(update)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "已套用優惠券，總金額已更新", Toast.LENGTH_SHORT).show();
                                                    fetchSummaryFromFirestore(); // ✨ 重新抓金額刷新畫面
                                                });
                                    }
                                });
                    }
                });
    }

    private void saveToPrefs() {
        SharedPreferences.Editor editor = getSharedPreferences("checkout_data", MODE_PRIVATE).edit();
        editor.putString("name", etName.getText().toString());
        editor.putString("cardNum", etCardNum.getText().toString());
        editor.putString("date", etDate.getText().toString());
        editor.putString("code", etVerificationCode.getText().toString());
        editor.apply();
    }

    private void putEditTextValues(Intent intent) {
        intent.putExtra("name", etName.getText().toString());
        intent.putExtra("cardNum", etCardNum.getText().toString());
        intent.putExtra("date", etDate.getText().toString());
        intent.putExtra("code", etVerificationCode.getText().toString());
    }

    // 建立簡化版 TextWatcher（只需實作 afterTextChanged）
    private abstract class SimpleTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}