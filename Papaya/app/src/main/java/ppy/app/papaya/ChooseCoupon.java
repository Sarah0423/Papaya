package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChooseCoupon extends AppCompatActivity {

    private RecyclerView rvCoupon;
    private CouponAdapter adapter;
    private List<CouponInfo> couponList;
    private ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_coupon);

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

    private void fetchCoupons() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("owned_coupons")
                .whereEqualTo("is_used", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<Long> ownedIndexes = new HashSet<>();

                    for (DocumentSnapshot ownedDoc : querySnapshot) {
                        Long ownedIndex = ownedDoc.getLong("coupon_index");
                        if (ownedIndex != null) {
                            ownedIndexes.add(ownedIndex);
                        }
                    }

                    if (ownedIndexes.isEmpty()) {
                        Log.d("COUPON_DEBUG", "沒有未使用的優惠券");
                        return;
                    }

                    db.collection("coupons")
                            .whereIn("coupon_index", new ArrayList<>(ownedIndexes))
                            .get()
                            .addOnSuccessListener(couponDocs -> {
                                couponList.clear();

                                for (DocumentSnapshot couponDoc : couponDocs) {
                                    String name = couponDoc.getString("coupon_name");
                                    String info = couponDoc.getString("coupon_info");
                                    String photo = couponDoc.getString("coupon_photo");
                                    String type = couponDoc.getString("coupon_type");

                                    couponList.add(new CouponInfo(name, info, photo, type));
                                    Log.d("COUPON_DEBUG", "加入優惠券: " + name);
                                }

                                Collections.sort(couponList, Comparator.comparing(CouponInfo::getCouponType));
                                adapter.notifyDataSetChanged();
                            });
                });
    }
}