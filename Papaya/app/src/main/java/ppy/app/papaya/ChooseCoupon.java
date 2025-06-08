package ppy.app.papaya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ChooseCoupon extends AppCompatActivity {
    // UI 與資料變數
    private RecyclerView rvCoupon;
    private CouponAdapter adapter;
    private List<CouponInfo> couponList;
    private ImageButton btnReturn;
    private TextView tvTotalCoupon;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_coupon);

        tvTotalCoupon = findViewById(R.id.tv_total_coupon);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn = findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseCoupon.this, CheckoutCart.class);
            startActivity(intent);
        });

        btnConfirm = findViewById(R.id.btn_confirm_coupon);
        btnConfirm.setOnClickListener(v -> {
            List<CouponInfo> selectedCoupons = adapter.getSelectedCoupons();
            if (selectedCoupons.isEmpty()) {
                Toast.makeText(this, "請先選擇優惠券", Toast.LENGTH_SHORT).show();
                return;
            }

            // 限制每種類型最多一張
            Map<String, CouponInfo> typeToCoupon = new HashMap<>();
            for (CouponInfo coupon : selectedCoupons) {
                typeToCoupon.put(coupon.getCouponType(), coupon);
            }
            List<CouponInfo> filteredCoupons = new ArrayList<>(typeToCoupon.values());

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            long totalAmount = getIntent().getLongExtra("total_amount", 0);
            AtomicLong totalDiscount = new AtomicLong(0);
            long[] shippingFee = {30};
            int[] completed = {0};
            int totalCoupons = filteredCoupons.size();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            for (CouponInfo selected : filteredCoupons) {
                String type = selected.getCouponType();
                String couponId = selected.getCouponId();

                db.collection("coupon").document(couponId).get().addOnSuccessListener(couponDoc -> {
                    Map<String, Object> value = (Map<String, Object>) couponDoc.get("coupon_value");

                    switch (type) {
                        case "discount_percent":
                            long threshold = ((Number) value.get("threshold")).longValue();
                            long percentOff = ((Number) value.get("percent_off")).longValue();
                            if (totalAmount >= threshold) {
                                totalDiscount.addAndGet(Math.round(totalAmount * percentOff / 100.0));
                            }
                            break;

                        case "discount_amount":
                            threshold = ((Number) value.get("threshold")).longValue();
                            long amount = ((Number) value.get("amount")).longValue();
                            if (totalAmount >= threshold) {
                                totalDiscount.addAndGet(amount);
                            }
                            break;

                        case "discount_delivery":
                            Boolean isFree = (Boolean) value.get("is_free");
                            if (isFree != null && isFree) {
                                shippingFee[0] = 0;
                            }
                            break;

                        case "buy_x_get_y":
                            long buy = ((Number) value.get("buy")).longValue();
                            long get = ((Number) value.get("get")).longValue();
                            String itemCategory = (String) value.get("item");

                            db.collection("users").document(uid).collection("cart_item").get()
                                    .addOnSuccessListener(cartDocs -> {
                                        List<Long> matchingPrices = new ArrayList<>();
                                        for (DocumentSnapshot item : cartDocs.getDocuments()) {
                                            String photo = item.getString("item_photo");
                                            Long quantity = item.getLong("item_quantity");
                                            Long price = item.getLong("item_price");
                                            if (photo != null && quantity != null && price != null && photo.contains(itemCategory)) {
                                                for (int i = 0; i < quantity; i++) {
                                                    matchingPrices.add(price);
                                                }
                                            }
                                        }

                                        if (matchingPrices.size() >= buy) {
                                            Collections.sort(matchingPrices);
                                            long discount = 0;
                                            for (int i = 0; i < get && i < matchingPrices.size(); i++) {
                                                discount += matchingPrices.get(i);
                                            }
                                            totalDiscount.addAndGet(discount);
                                        }

                                        checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("COUPON", "讀取 cart_item 失敗", e);
                                        checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons);
                                    });
                            return; // 防止重複呼叫 checkAndFinalize
                    }

                    // 非 buy_x_get_y 在這裡呼叫 finalize check
                    checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons);
                }).addOnFailureListener(e -> {
                    Log.e("COUPON", "讀取 coupon 失敗", e);
                    checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons);
                });
            }
        });

        rvCoupon = findViewById(R.id.rv_coupon);
        couponList = new ArrayList<>();
        adapter = new CouponAdapter(this, couponList);
        rvCoupon.setLayoutManager(new LinearLayoutManager(this));
        rvCoupon.setAdapter(adapter);

        fetchCoupons();
    }

    private void checkAndFinalize(String uid, long shippingFee, long discountAmount, int completed, int total) {
        Log.d("COUPON", "completed: " + completed + "/" + total + " discount: " + discountAmount);
        if (completed == total) {
            finalizeCouponUpdate(uid, shippingFee, discountAmount);
        }
    }

    private void finalizeCouponUpdate(String uid, long shippingFee, long discountAmount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("discount", discountAmount);
        updates.put("shipping_fee", shippingFee);

        db.collection("users").document(uid)
                .collection("cart_info").document("summary")
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "優惠券套用成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "更新失敗，請稍後再試", Toast.LENGTH_SHORT).show();
                    Log.e("COUPON", "Firestore 更新失敗", e);
                });
    }

    private void updateCouponDisplay() {
        Collections.sort(couponList, Comparator.comparing(CouponInfo::getCouponType));
        adapter.notifyDataSetChanged();
        tvTotalCoupon.setText("共有 " + couponList.size() + " 張優惠券");
    }

    private void fetchCoupons() {
        // coupon_index 對應的 Firestore 文件名稱
        Map<Long, String> indexToDocId = new HashMap<>();
        indexToDocId.put(1L, "FREE_DELIVERY");
        indexToDocId.put(2L, "DISCOUNT_30");
        indexToDocId.put(3L, "TOAST_BOGO");
        indexToDocId.put(4L, "POINTS_2");
        indexToDocId.put(5L, "DISCOUNT_10PERCENT_OVER100");
        indexToDocId.put(6L, "TOAST_BTGO");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("owned_coupons")
                .whereEqualTo("is_used", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<Long> ownedIndexes = new HashSet<>();
                    Date now = new Date();

                    for (DocumentSnapshot ownedDoc : querySnapshot.getDocuments()) {
                        Long ownedIndex = ownedDoc.getLong("coupon_index");
                        Date expireAt = ownedDoc.getDate("expire_at");
                        if (ownedIndex != null && expireAt != null && expireAt.after(now)) {
                            ownedIndexes.add(ownedIndex);
                        }
                    }

                    if (ownedIndexes.isEmpty()) {
                        tvTotalCoupon.setText("你目前沒有可用的優惠券");
                        return;
                    }

                    couponList.clear();
                    List<Long> indexList = new ArrayList<>(ownedIndexes);
                    final int totalToLoad = indexList.size();
                    final int[] loadedCount = {0};

                    for (Long index : indexList) {
                        String docId = indexToDocId.get(index);
                        if (docId == null) {
                            loadedCount[0]++;
                            continue;
                        }

                        db.collection("coupon").document(docId)
                                .get()
                                .addOnSuccessListener(couponDoc -> {
                                    if (couponDoc.exists()) {
                                        String name = couponDoc.getString("coupon_name");
                                        String info = couponDoc.getString("coupon_info");
                                        String photo = couponDoc.getString("coupon_photo");
                                        String type = couponDoc.getString("coupon_type");

                                        couponList.add(new CouponInfo(name, info, photo, type, couponDoc.getId()));
                                    }
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalToLoad) updateCouponDisplay();
                                })
                                .addOnFailureListener(e -> {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalToLoad) updateCouponDisplay();
                                });
                    }
                });
    }
}