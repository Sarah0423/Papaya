package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class DrinkInfoActivity extends AppCompatActivity {

    private ImageButton btnReturn, ibCurDrink, ibMinusNum, ibPlusNum;

    private TextView tvDrinkName, tvDrinkPrice, tvDrinkNum;
    private RadioGroup rgSugar, rgIce;
    private Button btnConfirm;

    private String drinkName, drinkPhoto;
    private int drinkIndex, drinkPrice, quantity = 1;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_drink_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvDrinkName = findViewById(R.id.tv_drink_name);
        tvDrinkPrice = findViewById(R.id.tv_drink_price);
        tvDrinkNum = findViewById(R.id.tv_drink_num);

        rgSugar = findViewById(R.id.rg_sugar);
        rgIce = findViewById(R.id.rg_ice);
        btnConfirm = findViewById(R.id.btn_add_to_cart);
        ibCurDrink = findViewById(R.id.ib_cur_drink);

        ibMinusNum = findViewById(R.id.ib_minus_num);
        ibPlusNum = findViewById(R.id.ib_plus_num);

        // 🔹 步驟 1：從 Intent 取得 index
        drinkIndex = getIntent().getIntExtra("beverage_index", -1);

        // 🔹 步驟 2：綁定元件
        btnReturn = findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrinkInfoActivity.this, MainActivity.class);
                intent.putExtra("tab_position", 1); // 傳給 MainActivity 的選單位置
                startActivity(intent);
            }
        });

        tvDrinkNum.setText(String.valueOf(quantity));

        ibMinusNum.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvDrinkNum.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "數量不能小於 1", Toast.LENGTH_SHORT).show();
            }
        });

        ibPlusNum.setOnClickListener(v -> {
            quantity++;
            tvDrinkNum.setText(String.valueOf(quantity));
        });

        // 🔹 步驟 3：查詢 Firestore 取得飲料資料
        db.collection("beverage")
                .whereEqualTo("beverage_index", drinkIndex)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                        // 把飲料資料拉出來
                        drinkName = doc.getString("beverage_name");
                        drinkPhoto = doc.getString("beverage_photo");
                        drinkPrice = doc.getLong("beverage_price").intValue(); // Firestore 是 Long，要轉 int
                        drinkPhoto = doc.getString("beverage_photo");

                        // 顯示到畫面上
                        int resId = getResources().getIdentifier(
                                drinkPhoto, "mipmap", getPackageName());
                        ibCurDrink.setImageResource(resId);
                        tvDrinkName.setText(drinkName);
                        tvDrinkPrice.setText("$" + drinkPrice);

                        String beverageType = doc.getString("beverage_type");

                        if ("sparkling".equals(beverageType)) {
                            // 設定固定甜度（例如：無糖）和冰塊（例如：常溫）
                            RadioButton rbSugarHalf = findViewById(R.id.rb_sugar_half);
                            RadioButton rbIceNormal = findViewById(R.id.rb_ice_normal);

                            rbSugarHalf.setChecked(true);
                            rbIceNormal.setChecked(true);

                            // 禁用所有甜度選項
                            for (int i = 0; i < rgSugar.getChildCount(); i++) {
                                rgSugar.getChildAt(i).setEnabled(false);
                            }

                            // 禁用所有冰塊選項
                            for (int i = 0; i < rgIce.getChildCount(); i++) {
                                rgIce.getChildAt(i).setEnabled(false);
                            }

                            Toast.makeText(this, "氣泡飲為固定甜度與冰塊", Toast.LENGTH_SHORT).show();
                        }


                        // 🔹 設定按鈕行為（要等查完後才能設）
                        btnConfirm.setOnClickListener(v -> {
                            int sugarId = rgSugar.getCheckedRadioButtonId();
                            int iceId = rgIce.getCheckedRadioButtonId();

                            if (sugarId == -1 || iceId == -1) {
                                Toast.makeText(this, "請選擇甜度與冰塊", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String sugar = ((RadioButton) findViewById(sugarId)).getText().toString();
                            String ice = ((RadioButton) findViewById(iceId)).getText().toString();
                            String selectedOption = sugar + ", " + ice;

                            HashMap<String, Object> item = new HashMap<>();
                            item.put("item_name", drinkName);
                            item.put("item_price", drinkPrice);
                            item.put("item_quantity", 1);
                            item.put("item_selected", selectedOption);
                            item.put("item_user_id", uid);
                            item.put("item_photo", drinkPhoto);

                            db.collection("users")
                                    .document(uid)
                                    .collection("cart_item")
                                    .add(item)
                                    .addOnSuccessListener(docRef -> {
                                        Toast.makeText(this, "已加入購物車！", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "加入失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });

                    } else {
                        Toast.makeText(this, "找不到飲料資料", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "查詢失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}