package ppy.app.papaya;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        String[] categories = {"Starters", "Asian", "Roasts", "Classci"};

        for (String title : categories) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
            tabLayout.addTab(tab);
        }

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rv_toast_item);
        // 初始化資料列表
        toastItemList = new ArrayList<>();
        // 初始化 Adapter 並設置給 RecyclerView
        adapter = new ToastAdapter(this, toastItemList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

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

        ibIndex = findViewById(R.id.ib_index);

        functionMenuContainer = findViewById(R.id.function_menu_container);
        LayoutInflater inflater = LayoutInflater.from(this);

        if (currentUser != null) {
            // 已登入 → 使用 function_menu.xml
            functionMenuView = inflater.inflate(R.layout.function_menu, functionMenuContainer, false);
        } else {
            // 未登入 → 使用 login_function_menu.xml
            functionMenuView = inflater.inflate(R.layout.login_function_menu, functionMenuContainer, false);
        }

        functionMenuContainer.addView(functionMenuView);
        functionMenuContainer.setVisibility(View.GONE);

        // 點 ib_index 顯示 function_menu
        ibIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                functionMenuContainer.setVisibility(View.VISIBLE);
            }
        });

        TextView tvUserName = functionMenuView.findViewById(R.id.tv_user_name);
        ImageView ivUserImg = functionMenuView.findViewById(R.id.iv_user_img);

        if (currentUser != null) {
            String email = currentUser.getEmail();
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            String avatar = document.getString("avatar");

                            if (tvUserName != null && name != null) {
                                tvUserName.setText(name);
                            }

                            if (ivUserImg != null) {
                                int imageResId;
                                if (avatar != null) {
                                    // 取得對應資源 id
                                    imageResId = getResources().getIdentifier(avatar, "mipmap", getPackageName());
                                    if (imageResId == 0) {
                                        // 如果找不到，使用預設圖
                                        imageResId = R.mipmap.login_usagi;
                                    }
                                } else {
                                    imageResId = R.mipmap.login_usagi;
                                }
                                ivUserImg.setImageResource(imageResId);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "讀取用戶資料失敗", Toast.LENGTH_SHORT).show();
                    });
        }

        Button btnRegister = functionMenuView.findViewById(R.id.btn_register_function_menu);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Register.class);
                    startActivity(intent);
                }
            });
        }

        Button btnSignin = functionMenuView.findViewById(R.id.btn_signin_function_menu);

        if(btnSignin != null) {
            btnSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            });
        }


        boolean showFunctionMenu = getIntent().getBooleanExtra("SHOW_FUNCTION_MENU", false);
        if (showFunctionMenu) {
            functionMenuContainer.setVisibility(View.VISIBLE);
        }

        LinearLayout linearLogout = functionMenuView.findViewById(R.id.logout_layout);
        if(linearLogout != null) {
            linearLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            });
        }

        LinearLayout linearOrderLogin = functionMenuView.findViewById(R.id.order_layout);

        linearOrderLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

        LinearLayout linearBranchLogin = functionMenuView.findViewById(R.id.branch_layout);
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

        LinearLayout linearDeliveryLogin = functionMenuView.findViewById(R.id.delivery_layout);
        linearDeliveryLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });


        LinearLayout linearCustomerServiceLogin = functionMenuView.findViewById(R.id.customer_service_layout);
        linearCustomerServiceLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

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