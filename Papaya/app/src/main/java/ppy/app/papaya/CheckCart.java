package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CheckCart extends AppCompatActivity {

    private ImageButton btnReturn;

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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckCart.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cart_item")
                .whereEqualTo("item_user_id", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CartItem> cartItemList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem item = document.toObject(CartItem.class);
                        item.setFirestoreId(document.getId());
                        cartItemList.add(item);
                    }
                    setupRecyclerView(cartItemList);
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error fetching cart items", e));

        TextView tvTotal = findViewById(R.id.tv_total);

        db.collection("cart_info")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long totalPrice = documentSnapshot.getLong("total_price") != null ? documentSnapshot.getLong("total_price") : 0;
                        tvTotal.setText("$" + totalPrice);
                    } else {
                        tvTotal.setText("$0");
                    }
                })
                .addOnFailureListener(e -> {
                    tvTotal.setText("讀取總價失敗");
                    Log.e("FIRESTORE", "Error fetching total price", e);
                });

    }

    private void setupRecyclerView(List<CartItem> cartItemList) {
        RecyclerView recyclerView = findViewById(R.id.rv_item_in_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView tvTotal = findViewById(R.id.tv_total);
        CartItemAdapter adapter = new CartItemAdapter(this, cartItemList, tvTotal);
        recyclerView.setAdapter(adapter);
    }

}