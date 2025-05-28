package ppy.app.papaya;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ToastItem> toastItemList;
    private ToastAdapter adapter;
    private FrameLayout functionMenuContainer;
    private View functionMenuView;
    private ImageButton ibIndex;



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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("toast") // ← 替換成你的資料集名稱
                .orderBy("toast_index")
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


        functionMenuContainer = findViewById(R.id.function_menu_container);
        ibIndex = findViewById(R.id.ib_index);

        // 用 LayoutInflater 把 function_menu.xml 塞進 container
        LayoutInflater inflater = LayoutInflater.from(this);
        functionMenuView = inflater.inflate(R.layout.login_function_menu, functionMenuContainer, false);
        functionMenuContainer.addView(functionMenuView);
        functionMenuContainer.setVisibility(View.GONE);

        // 點 ib_index 顯示 function_menu
        ibIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionMenuContainer.setVisibility(View.VISIBLE);
            }
        });

        Button btnRegister = functionMenuView.findViewById(R.id.btn_register_function_menu);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
        });

        Button btnSignin = functionMenuView.findViewById(R.id.btn_signin_function_menu);

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });


        boolean showFunctionMenu = getIntent().getBooleanExtra("SHOW_FUNCTION_MENU", false);
        if (showFunctionMenu) {
            functionMenuContainer.setVisibility(View.VISIBLE);
        }

        LinearLayout linearOrderLogin = functionMenuView.findViewById(R.id.order_login_layout);
        linearOrderLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

        LinearLayout linearBranchLogin = functionMenuView.findViewById(R.id.branch_login_layout);
        linearBranchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Branch_info.class);
                startActivity(intent);
            }
        });

        LinearLayout linearDailyLogin = functionMenuView.findViewById(R.id.daily_spin_layout);
        linearDailyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

        LinearLayout linearDeliveryLogin = functionMenuView.findViewById(R.id.delivery_login_layout);
        linearDeliveryLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });


        LinearLayout linearCustomerServiceLogin = functionMenuView.findViewById(R.id.customer_service_login_layout);
        linearCustomerServiceLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });


        /*View functionMenuView = getLayoutInflater().inflate(R.layout.function_menu, null);
        LinearLayout linearBranch = functionMenuView.findViewById(R.id.branch_layout);
        linearBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "點擊了，準備跳轉", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Branch_info.class);
                startActivity(intent);
            }
        });*/




    }

    // 點擊外面會從function_menu回到主頁
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (functionMenuContainer.getVisibility() == View.VISIBLE) {
                Rect outRect = new Rect();
                functionMenuContainer.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    functionMenuContainer.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }





}
