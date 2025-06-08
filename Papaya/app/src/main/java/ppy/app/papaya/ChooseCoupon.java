package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;
import java.util.HashMap;

public class ChooseCoupon extends AppCompatActivity {

    private RecyclerView rvCoupon;
    private CouponAdapter adapter;
    private List<CouponInfo> couponList;
    private ImageButton btnReturn;
    private TextView tvTotalCoupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d("COUPON_DEBUG", "測試 Log 是否有出現1");
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

        rvCoupon = findViewById(R.id.rv_coupon);
        couponList = new ArrayList<>();
        adapter = new CouponAdapter(this, couponList);
        rvCoupon.setLayoutManager(new LinearLayoutManager(this));
        rvCoupon.setAdapter(adapter);

        fetchCoupons();
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

        //Log.d("COUPON_DEBUG", "測試 Log 是否有出現");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("owned_coupons")
                .whereEqualTo("is_used", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("COUPON_DEBUG", "成功抓到 owned_coupons，共 " + querySnapshot.size() + " 筆");
                    Set<Long> ownedIndexes = new HashSet<>();

                    for (DocumentSnapshot ownedDoc : querySnapshot) {
                        Long ownedIndex = ownedDoc.getLong("coupon_index");
                        Log.d("COUPON_DEBUG", "owned coupon_index: " + ownedIndex);
                        if (ownedIndex != null) {
                            ownedIndexes.add(ownedIndex);
                        }
                    }

                    if (ownedIndexes.isEmpty()) {
                        Log.d("COUPON_DEBUG", "沒有未使用的優惠券");
                        tvTotalCoupon.setText("你目前沒有可用的優惠券");
                        return;
                    }

                    couponList.clear(); // 清空舊資料
                    List<Long> indexList = new ArrayList<>(ownedIndexes);
                    final int totalToLoad = indexList.size();
                    final int[] loadedCount = {0};

                    for (Long index : indexList) {
                        String docId = indexToDocId.get(index);
                        if (docId == null) {
                            Log.w("COUPON_DEBUG", "無法對應的 coupon_index: " + index);
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

                                        couponList.add(new CouponInfo(name, info, photo, type));
                                        Log.d("COUPON_DEBUG", "加入優惠券: " + name);
                                    } else {
                                        Log.w("COUPON_DEBUG", "找不到文件: " + docId);
                                    }

                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalToLoad) {
                                        updateCouponDisplay();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("COUPON_DEBUG", "讀取文件失敗: " + docId, e);
                                    loadedCount[0]++;
                                    if (loadedCount[0] == totalToLoad) {
                                        updateCouponDisplay();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("COUPON_DEBUG", "抓 owned_coupons 失敗", e);
                });

    }
}