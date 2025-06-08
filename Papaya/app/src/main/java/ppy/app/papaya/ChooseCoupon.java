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
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid)
                    .collection("cart_info").document("summary")
                    .get()
                    .addOnSuccessListener(docSnapshot -> {
                        Long totalAmount = docSnapshot.getLong("total_price");
                        if (totalAmount == null) {
                            Toast.makeText(this, "無法取得總金額", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        processCoupons(uid, totalAmount);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "讀取總金額失敗", Toast.LENGTH_SHORT).show();
                        Log.e("COUPON", "讀取 cart_info.summary 失敗", e);
                    });
        });

        rvCoupon = findViewById(R.id.rv_coupon);
        couponList = new ArrayList<>();
        adapter = new CouponAdapter(this, couponList);
        rvCoupon.setLayoutManager(new LinearLayoutManager(this));
        rvCoupon.setAdapter(adapter);

        fetchCoupons();
    }

    private void processCoupons(String uid, long totalAmount) {
        List<CouponInfo> selectedCoupons = adapter.getSelectedCoupons();
        if (selectedCoupons.isEmpty()) {
            Toast.makeText(this, "請先選擇優惠券", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, CouponInfo> typeToCoupon = new HashMap<>();
        for (CouponInfo coupon : selectedCoupons) {
            typeToCoupon.put(coupon.getCouponType(), coupon);
        }
        List<CouponInfo> filteredCoupons = new ArrayList<>(typeToCoupon.values());

        AtomicLong totalDiscount = new AtomicLong(0);
        long[] shippingFee = {30};
        int[] completed = {0};
        int totalCoupons = filteredCoupons.size();
        final String[] usedDeliveryCouponId = {null};
        final String[] usedDiscountCouponId = {null};

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
                            usedDiscountCouponId[0] = selected.getOwnedCouponId();
                        }
                        checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                                usedDiscountCouponId[0], usedDeliveryCouponId[0]);
                        break;

                    case "discount_amount":
                        threshold = ((Number) value.get("threshold")).longValue();
                        long amount = ((Number) value.get("amount")).longValue();
                        if (totalAmount >= threshold) {
                            totalDiscount.addAndGet(amount);
                            usedDiscountCouponId[0] = selected.getOwnedCouponId();
                        }
                        checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                                usedDiscountCouponId[0], usedDeliveryCouponId[0]);
                        break;

                    case "discount_delivery":
                        Boolean isFree = (Boolean) value.get("is_free");
                        if (isFree != null && isFree) {
                            shippingFee[0] = 0;
                            usedDeliveryCouponId[0] = selected.getOwnedCouponId();
                        }
                        checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                                usedDiscountCouponId[0], usedDeliveryCouponId[0]);
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
                                        usedDiscountCouponId[0] = selected.getOwnedCouponId();
                                    }

                                    checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                                            usedDiscountCouponId[0], usedDeliveryCouponId[0]);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("COUPON", "讀取 cart_item 失敗", e);
                                    checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                                            usedDiscountCouponId[0], usedDeliveryCouponId[0]);
                                });
                        break;
                }
            }).addOnFailureListener(e -> {
                Log.e("COUPON", "讀取 coupon 失敗", e);
                checkAndFinalize(uid, shippingFee[0], totalDiscount.get(), ++completed[0], totalCoupons,
                        usedDiscountCouponId[0], usedDeliveryCouponId[0]);
            });
        }
    }


    private void checkAndFinalize(String uid, long shippingFee, long discountAmount,
                                  int completed, int total,
                                  String usedDiscountCouponId, String usedDeliveryCouponId) {
        if (completed == total) {
            finalizeCouponUpdate(uid, shippingFee, discountAmount, usedDiscountCouponId, usedDeliveryCouponId);
        }
    }

    private void finalizeCouponUpdate(String uid, long shippingFee, long discountAmount,
                                      String usedDiscountCouponId, String usedDeliveryCouponId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("discount", discountAmount);
        updates.put("shipping_fee", shippingFee);

        if (usedDiscountCouponId != null)
            updates.put("coupon_discount", usedDiscountCouponId);
        if (usedDeliveryCouponId != null)
            updates.put("coupon_delivery", usedDeliveryCouponId);

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        Map<Long, String> indexToDocId = new HashMap<>();
        indexToDocId.put(1L, "FREE_DELIVERY");
        indexToDocId.put(2L, "DISCOUNT_30");
        indexToDocId.put(3L, "TOAST_BOGO");
        indexToDocId.put(4L, "POINTS_2");
        indexToDocId.put(5L, "DISCOUNT_10PERCENT_OVER100");
        indexToDocId.put(6L, "TOAST_BTGO");

        db.collection("users").document(uid).collection("owned_coupons")
                .whereEqualTo("is_used", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> validCoupons = new ArrayList<>();
                    Date now = new Date();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Date expireAt = doc.getDate("expire_at");
                        if (expireAt != null && expireAt.after(now)) {
                            validCoupons.add(doc); // 每張保留，不去重複
                        }
                    }

                    if (validCoupons.isEmpty()) {
                        tvTotalCoupon.setText("你目前沒有可用的優惠券");
                        return;
                    }

                    couponList.clear();
                    final int totalToLoad = validCoupons.size();
                    final int[] loadedCount = {0};

                    for (DocumentSnapshot ownedDoc : validCoupons) {
                        Long index = ownedDoc.getLong("coupon_index");
                        String ownedId = ownedDoc.getId(); // ⭐ 每張券的唯一 ID
                        if (index == null) {
                            loadedCount[0]++;
                            continue;
                        }

                        String couponDocId = indexToDocId.get(index);
                        if (couponDocId == null) {
                            loadedCount[0]++;
                            continue;
                        }

                        db.collection("coupon").document(couponDocId)
                                .get()
                                .addOnSuccessListener(couponDoc -> {
                                    if (couponDoc.exists()) {
                                        String name = couponDoc.getString("coupon_name");
                                        String info = couponDoc.getString("coupon_info");
                                        String photo = couponDoc.getString("coupon_photo");
                                        String type = couponDoc.getString("coupon_type");

                                        CouponInfo coupon = new CouponInfo(name, info, photo, type, couponDoc.getId());
                                        coupon.setOwnedCouponId(ownedId);
                                        Date expireAt = ownedDoc.getDate("expire_at");
                                        if (expireAt != null) {
                                            coupon.setExpireAt(new Timestamp(expireAt));
                                        }
                                        couponList.add(coupon);
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