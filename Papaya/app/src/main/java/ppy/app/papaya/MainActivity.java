package ppy.app.papaya;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvToast;
    private RecyclerView rvAlarm;
    private List<ToastItem> toastItemList;
    private List<DrinkItem> drinkItemList;
    private ToastAdapter adapter;
    private DrinkAdapter drinkAdapter;
    private FrameLayout functionMenuContainer;
    private View functionMenuView;
    private ImageButton ibIndex;
    private LinearLayout llCreateToast;
    private LinearLayout llBtnLoginRegister;
    private LinearLayout pointsLayout;
    private LinearLayout logoutLayout;
    private LinearLayout myFavoriteLayout;
    private LinearLayout UpdateToPro;
    private ImageView llBtnCartCircle;
    private TextView tvItemNum;
    private Button btnGotoCart;
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

        llBtnCartCircle = findViewById(R.id.circularImageView);
        tvItemNum = findViewById(R.id.tv_item_num);

        llCreateToast = findViewById(R.id.ll_create_toast);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        String[] categories = {"Toast", "Drink"};

        for (String title : categories) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
            tabLayout.addTab(tab);
        }

        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        rvToast = findViewById(R.id.rv_main_item);
        // 初始化資料列表
        toastItemList = new ArrayList<>();
        drinkItemList = new ArrayList<>();
        // 初始化 Adapter 並設置給 RecyclerView
        adapter = new ToastAdapter(this, toastItemList);
        drinkAdapter = new DrinkAdapter(this, drinkItemList);

        rvToast.setAdapter(adapter);
        rvToast.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));

        db.collection("toast")
                .orderBy("toast_index")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ToastItem item = document.toObject(ToastItem.class);
                        toastItemList.add(item);
                    }
                    adapter.notifyDataSetChanged(); // 通知資料更新
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "吐司資料讀取失敗", Toast.LENGTH_SHORT).show();
                });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) { // Toast
                    llCreateToast.setVisibility(View.VISIBLE);
                    rvToast.setAdapter(adapter); // 正確使用 ToastAdapter
                    toastItemList.clear(); // ✅ 清空正確的列表

                    db.collection("toast")
                            .orderBy("toast_index")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    ToastItem item = document.toObject(ToastItem.class);
                                    toastItemList.add(item);
                                }
                                adapter.notifyDataSetChanged(); // 通知資料更新
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "吐司資料讀取失敗", Toast.LENGTH_SHORT).show();
                            });
                } else if (position == 1) { // Drink
                    llCreateToast.setVisibility(View.INVISIBLE);
                    rvToast.setAdapter(drinkAdapter); // ✅ 使用 drinkAdapter

                    drinkItemList.clear();

                    db.collection("beverage")
                            .orderBy("beverage_index")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    DrinkItem item = document.toObject(DrinkItem.class);
                                    drinkItemList.add(item);
                                }
                                drinkAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "飲料資料讀取失敗", Toast.LENGTH_SHORT).show();
                            });
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        int tabPosition = getIntent().getIntExtra("tab_position", -1);
        if (tabPosition != -1 && tabPosition < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (tab != null) {
                tab.select();  // 自動選擇該頁籤
            }
        }



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
                Intent intent = new Intent(MainActivity.this, BranchInfo.class);
                startActivity(intent);
            }
        });

        LinearLayout linearDailyLogin = functionMenuView.findViewById(R.id.daily_spin_layout);
        linearDailyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Intent intent = new Intent(MainActivity.this, DailySpinActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        });

        LinearLayout linearFavoriteLogin = functionMenuView.findViewById(R.id.my_favorite_layout);
        if(linearFavoriteLogin != null){
            linearFavoriteLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, FavoriteBranch.class);
                    startActivity(intent);
                }
            });
        }

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
                    Intent intent = new Intent(MainActivity.this, CustomerServiceActivity.class);
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

        ImageButton reminderBtn = findViewById(R.id.ib_notification);
        FrameLayout alarmOverlay = findViewById(R.id.alarm_overlay);
        rvAlarm = findViewById(R.id.rv_alarm);
        rvAlarm.setLayoutManager(new LinearLayoutManager(this));
        rvAlarm.setAdapter(adapter);

// 加上 14dp 的間隔
        int spaceInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        rvAlarm.addItemDecoration(new VerticalSpaceItemDecoration(spaceInPx));


        List<Alarm> alarmList = new ArrayList<>();
        AlarmAdapter adapter = new AlarmAdapter(alarmList);
        rvAlarm.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        rvAlarm.setAdapter(adapter);

        reminderBtn.setOnClickListener(v -> {
            if (!checkLogin()) return;

            if (alarmOverlay.getVisibility() == View.VISIBLE) {
                alarmOverlay.setVisibility(View.GONE);
            } else {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY, -24);
                Timestamp time24HoursAgo = new Timestamp(cal.getTime());

                db.collection("users")
                        .document(uid)
                        .collection("alarms")
                        .whereGreaterThan("alarms_time", time24HoursAgo)
                        .orderBy("alarms_time")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            alarmList.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                String photo = doc.getString("alarms_photo");
                                String type = doc.getString("alarms_type");
                                String info = doc.getString("alarms_info");

                                alarmList.add(new Alarm(photo, type, info));
                            }
                            adapter.notifyDataSetChanged();
                            alarmOverlay.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "載入提醒失敗", Toast.LENGTH_SHORT).show();
                        });
            }
        });

// 可選：點擊 overlay 背景也收起
        alarmOverlay.setOnClickListener(v -> alarmOverlay.setVisibility(View.GONE));

        btnGotoCart = findViewById(R.id.btn_goto_cart);
        btnGotoCart.setOnClickListener(v -> {
            if (!checkLogin()) return;

            // 有登入才執行後續
            Intent intent = new Intent(MainActivity.this, CheckCartActivity.class);
            startActivity(intent);
        });
    }

    private boolean checkLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "請先登入帳號", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference cartInfoRef = db.collection("users")
                .document(userUid)
                .collection("cart_info")
                .document("summary");

        // 用 snapshotListener 監聽 summary 資料變化
        cartInfoListener = cartInfoRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                llBtnCartCircle.setVisibility(View.GONE);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long totalQuantity = documentSnapshot.getLong("total_quantity");
                Long totalPrice = documentSnapshot.getLong("total_price");
                btnGotoCart.setText("$" + totalPrice);
                if (totalQuantity != null && totalQuantity > 0) {
                    llBtnCartCircle.setVisibility(View.VISIBLE);
                    tvItemNum.setText(String.valueOf(totalQuantity));
                } else {
                    llBtnCartCircle.setVisibility(View.GONE);
                }
            } else {
                llBtnCartCircle.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userUid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference cartInfoRef = db.collection("users")
                .document(userUid)
                .collection("cart_info")
                .document("summary");  // ← 你實際使用的 document ID，確認一下是不是叫這個

        cartInfoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long totalQuantity = documentSnapshot.getLong("total_quantity") != null ? documentSnapshot.getLong("total_quantity") : 0;
                long totalPrice = documentSnapshot.getLong("total_price") != null ? documentSnapshot.getLong("total_price") : 0;
                btnGotoCart.setText("$" + totalPrice);
                if (totalQuantity <= 0) {
                    llBtnCartCircle.setVisibility(View.GONE);
                } else {
                    llBtnCartCircle.setVisibility(View.VISIBLE);
                    tvItemNum.setText(String.valueOf(totalQuantity));
                }
            } else {
                llBtnCartCircle.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            llBtnCartCircle.setVisibility(View.GONE);
            e.printStackTrace();
        });

    }

    ListenerRegistration cartInfoListener;

    @Override
    protected void onStop() {
        super.onStop();
        if (cartInfoListener != null) {
            cartInfoListener.remove();
        }
    }



}