package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckCartActivity extends AppCompatActivity {

    private ImageButton btnReturn;
    private TextView tvTotal;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uid;
    private LinearLayout llCommit;
    private long totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn = findViewById(R.id.btn_return);
        tvTotal = findViewById(R.id.tv_total);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(CheckCartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        listenToCartItems();

        llCommit = findViewById(R.id.ll_commit);
        llCommit.setOnClickListener(v -> {
                Intent intent = new Intent(CheckCartActivity.this, CheckoutCart.class);
                intent.putExtra("totalAmount", totalAmount); // 傳 long 數字
                startActivity(intent);
                finish();
        });
    }

    private void listenToCartItems() {
        db.collection("users")
                .document(uid)
                .collection("cart_item")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("FIRESTORE", "Listen failed.", e);
                        return;
                    }

                    List<CartItem> cartItemList = new ArrayList<>();
                    int totalQuantity = 0;
                    totalAmount = 0;

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            CartItem item = doc.toObject(CartItem.class);
                            item.setFirestoreId(doc.getId());
                            cartItemList.add(item);

                            Long price = doc.getLong("item_price");
                            Long quantity = doc.getLong("item_quantity");

                            if (price != null && quantity != null) {
                                totalQuantity += quantity;
                                totalAmount += price * quantity;
                            }
                        }
                    }

                    // 更新畫面
                    tvTotal.setText("$" + totalAmount);

                    db.collection("users")
                            .document(uid)
                            .collection("cart_info")
                            .document("summary")
                            .set(new CartInfo(totalAmount, totalQuantity));


                    setupRecyclerView(cartItemList);
                });
    }


    private void setupRecyclerView(List<CartItem> cartItemList) {
        RecyclerView recyclerView = findViewById(R.id.rv_item_in_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CartItemAdapter adapter = new CartItemAdapter(this, cartItemList, tvTotal);
        recyclerView.setAdapter(adapter);
    }
}

