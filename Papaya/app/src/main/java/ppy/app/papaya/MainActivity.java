package ppy.app.papaya;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private LinearLayout llBtnLoginRegister;
    private LinearLayout pointsLayout;
    private LinearLayout logoutLayout;
    private LinearLayout myFavoriteLayout;
    private LinearLayout UpdateToPro;


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

        functionMenuView = inflater.inflate(R.layout.function_menu, functionMenuContainer, false);
        llBtnLoginRegister = functionMenuView.findViewById(R.id.ll_btn_login_register);
        pointsLayout = functionMenuView.findViewById(R.id.points_layout);
        logoutLayout = functionMenuView.findViewById(R.id.logout_layout);
        myFavoriteLayout = functionMenuView.findViewById(R.id.my_favorite_layout);

        // 控制顯示按鈕
        if (currentUser != null) {
            llBtnLoginRegister.setVisibility(View.GONE);
            pointsLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.VISIBLE);
            myFavoriteLayout.setVisibility(View.VISIBLE);
        }else{
            llBtnLoginRegister.setVisibility(View.VISIBLE);
            pointsLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.GONE);
            myFavoriteLayout.setVisibility(View.GONE);
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

        ivUserImg.setOnClickListener(v -> {
            if (currentUser != null) {
                openChooseAvatarDialog(currentUser.getEmail(), tvUserName, ivUserImg);
            }
        });

        tvUserName.setOnClickListener(v -> {
            if (currentUser != null) {
                openEditUserNameDialog(currentUser.getEmail(), tvUserName);
            }
        });

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
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, DailySpin.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
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
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, CustomerService.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        });

        LinearLayout linearUpdateToPro = functionMenuView.findViewById(R.id.ll_update_to_pro);
        linearUpdateToPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, LoginProInfo.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        });

        ImageButton reminderBtn = findViewById(R.id.ib_notification); // 替換為你對應的 id
        reminderBtn.setOnClickListener(v -> {
            String prize = getSharedPreferences("PapayaPrefs", MODE_PRIVATE)
                    .getString("lastPrize", "目前尚未中獎");

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("中獎提醒")
                    .setMessage(prize)
                    .setPositiveButton("確定", null)
                    .show();
        });


    }

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

    private void openChooseAvatarDialog(String email, TextView tvUserName, ImageView ivUserImg) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_user_img);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        ImageButton ibUnknown = dialog.findViewById(R.id.ib_unknown_user_img);
        ImageButton ibChii = dialog.findViewById(R.id.ib_chii_user_img);
        ImageButton ibHachi = dialog.findViewById(R.id.ib_hachi_user_img);
        ImageButton ibUsagi = dialog.findViewById(R.id.ib_usagi_user_img);

        View.OnClickListener listener = v -> {
            String selectedAvatarName;
            int id = v.getId();
            if (id == R.id.ib_unknown_user_img) {
                selectedAvatarName = "login_usagi";
            } else if (id == R.id.ib_chii_user_img) {
                selectedAvatarName = "user_chii";
            } else if (id == R.id.ib_hachi_user_img) {
                selectedAvatarName = "user_hachi";
            } else if (id == R.id.ib_usagi_user_img) {
                selectedAvatarName = "user_usagi";
            }else {
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            db.collection("users")
                                    .document(doc.getId())
                                    .update("avatar", selectedAvatarName)
                                    .addOnSuccessListener(aVoid -> {
                                        int imageResId = getResources().getIdentifier(selectedAvatarName, "mipmap", getPackageName());
                                        ivUserImg.setImageResource(imageResId);
                                        Toast.makeText(this, "頭像更新成功", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show());
                        }
                    });
            dialog.dismiss();
        };

        ibUnknown.setOnClickListener(listener);
        ibChii.setOnClickListener(listener);
        ibHachi.setOnClickListener(listener);
        ibUsagi.setOnClickListener(listener);

        dialog.show();
    }


    private void openEditUserNameDialog(String email, TextView tvUserName) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_name);

        EditText etNewName = dialog.findViewById(R.id.et_new_name);
        Button btnSave = dialog.findViewById(R.id.btn_save_name);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> {
            String newName = etNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "名字不能空白", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            db.collection("users")
                                    .document(doc.getId())
                                    .update("name", newName)
                                    .addOnSuccessListener(aVoid -> {
                                        tvUserName.setText(newName);
                                        Toast.makeText(this, "名稱更新成功", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "更新失敗", Toast.LENGTH_SHORT).show());
                        }
                    });
            dialog.dismiss();
        });

        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("CartPrefs", MODE_PRIVATE);
        int cartCount = prefs.getInt("cart_count", 0);
        int cartTotal = prefs.getInt("cart_total", 0);
        TextView tvItemNum = findViewById(R.id.tv_item_num);
        Button btnGotoCart = findViewById(R.id.btn_goto_cart);
        tvItemNum.setText(String.valueOf(cartCount));
        btnGotoCart.setText("$" + cartTotal);
    }

}