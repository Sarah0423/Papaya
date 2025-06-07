package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.app.AlertDialog;
import android.widget.TextView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class DeliveryInfo extends AppCompatActivity {
    private ImageButton btnReturn;
    private EditText etName, etPhone, etAddress;
    private CheckBox cbTakeMyself;
    private Button btnConfirm;
    private TextView tvAddressLabel;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivery_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化元件
        etName = findViewById(R.id.et_realname);
        etPhone = findViewById(R.id.et_real_phone);
        etAddress = findViewById(R.id.et_real_address);
        cbTakeMyself = findViewById(R.id.cb_take_myself);
        btnConfirm = findViewById(R.id.btn_confirm_delivery);
        btnReturn = findViewById(R.id.btn_return_to_checkout);
        tvAddressLabel = findViewById(R.id.tv_delivery_address);

        TextView tvSelectedBranch = findViewById(R.id.tv_selected_branch);
        Button btnChooseBranch = findViewById(R.id.btn_choose_branch);

        final String[] selectedBranch = {null}; // 儲存選到的分店

        // Firebase 初始化
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // 假設你已登入

        // 返回按鈕
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(DeliveryInfo.this, CheckoutCart.class);
            startActivity(intent);
            finish();
        });

        // 確認按鈕點擊事件
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            boolean isTakeMyself = cbTakeMyself.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "請輸入姓名", Toast.LENGTH_SHORT).show();
                return;
            }

            // 如果不是自取，地址必填
            if (!isTakeMyself && address.isEmpty()) {
                Toast.makeText(this, "請輸入地址或選擇自取", Toast.LENGTH_SHORT).show();
                return;
            }

            // 自取的話設為 "自取"
            if (isTakeMyself) {
                address = "自取";
            }

            String phone = etPhone.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "請輸入連絡電話", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> deliveryInfo = new HashMap<>();
            deliveryInfo.put("name", name);
            deliveryInfo.put("phone", phone);
            deliveryInfo.put("address", address);
            deliveryInfo.put("branch", selectedBranch != null ? selectedBranch : "未選擇");

            cbTakeMyself.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    etAddress.setVisibility(View.GONE);
                    tvAddressLabel.setVisibility(View.GONE);
                } else {
                    etAddress.setVisibility(View.VISIBLE);
                    tvAddressLabel.setVisibility(View.VISIBLE);
                }
            });

            // 寫入 Firestore
            db.collection("users")
                    .document(userId)
                    .collection("delivery")
                    .document("delivery_info")
                    .set(deliveryInfo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "配送資訊已儲存", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DeliveryInfo.this, CheckoutCart.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "儲存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnChooseBranch.setOnClickListener(v -> {
            db.collection("map").get().addOnSuccessListener(querySnapshot -> {
                List<String> branchList = new ArrayList<>();
                List<String> branchIdList = new ArrayList<>();

                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String city = doc.getString("map_city");
                    String name = doc.getString("map_name");
                    branchList.add(city + " - " + name);
                    branchIdList.add(doc.getId()); // 若需要存 branchId
                }

                String[] branches = branchList.toArray(new String[0]);

                new AlertDialog.Builder(DeliveryInfo.this)
                        .setTitle("選擇分店")
                        .setSingleChoiceItems(branches, -1, (dialog, which) -> {
                            selectedBranch[0] = branches[which];
                        })
                        .setPositiveButton("確定", (dialog, which) -> {
                            if (selectedBranch[0] != null) {
                                tvSelectedBranch.setText("已選擇：" + selectedBranch[0]);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "讀取分店失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

    }
}