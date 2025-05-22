package ppy.app.papaya;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ToastItem> toastItemList;
    private ToastAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        String[] categories = {"Starters", "Asian", "Roasts", "Classci"};

        for (String title : categories) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
            tabLayout.addTab(tab);
        }

        recyclerView = findViewById(R.id.rv_toast_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化資料列表
        toastItemList = new ArrayList<>();
        // 初始化 Adapter 並設置給 RecyclerView
        adapter = new ToastAdapter(this, toastItemList);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("toast") // ← 替換成你的資料集名稱
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ToastItem item = document.toObject(ToastItem.class);
                        toastItemList.add(item);
                    }
                    adapter.notifyDataSetChanged(); // ← 通知資料更新
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    // 可以加上錯誤訊息提示
                    Toast.makeText(MainActivity.this, "資料讀取失敗", Toast.LENGTH_SHORT).show();
                });


    }

}
