package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoriteBranch extends AppCompatActivity {

    private ImageButton btnReturn;
    private Button btnEditFavorite;
    private RecyclerView rvFavoriteBranch;
    private FavoriteAdapter adapter;
    private List<MapItem> branchList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite_branch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        btnReturn = findViewById(R.id.btn_return);

        RecyclerView recyclerView = findViewById(R.id.rv_favorite_branch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(12));

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoriteBranch.this, MainActivity.class);
                intent.putExtra("SHOW_FUNCTION_MENU", true);
                startActivity(intent);
                finish();
            }
        });

        List<MapItem> favoriteList = new ArrayList<>();
        FavoriteAdapter adapter = new FavoriteAdapter(favoriteList);
        recyclerView.setAdapter(adapter);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            CollectionReference favoriteRef = db.collection("users").document(uid).collection("favorite");

            favoriteRef.get().addOnSuccessListener(favSnapshot -> {
                List<String> favoriteNames = new ArrayList<>();
                for (DocumentSnapshot doc : favSnapshot) {
                    favoriteNames.add(doc.getId());
                }

                if (!favoriteNames.isEmpty()) {
                    db.collection("map").whereIn("map_name", favoriteNames).get().addOnSuccessListener(mapSnapshot -> {
                        for (DocumentSnapshot doc : mapSnapshot) {
                            String img = doc.getString("map_img");
                            String name = doc.getString("map_name");
                            String tel = doc.getString("map_tel");
                            String address = doc.getString("map_address");
                            favoriteList.add(new MapItem(img, name, tel, address));
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> Log.e("Favorite", "Error getting map data", e));
                }
            }).addOnFailureListener(e -> Log.e("Favorite", "Error getting favorite list", e));
        }
    }

}