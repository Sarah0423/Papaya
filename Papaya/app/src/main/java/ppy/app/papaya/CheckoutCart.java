package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.List;

public class CheckoutCart extends AppCompatActivity {

    private ImageButton btnReturn;
    private Button btnDeliveryInfo;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uid;
    private TextView total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        btnReturn = findViewById(R.id.btn_return_to_cart);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, CheckCartActivity.class);
            startActivity(intent);
            finish();
        });

        btnDeliveryInfo = findViewById(R.id.btn_write_delivery_info);
        btnDeliveryInfo.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutCart.this, DeliveryInfo.class);
            startActivity(intent);
            finish();
        });

        total = findViewById(R.id.rginpok5lmng);
        long totalAmount = getIntent().getLongExtra("totalAmount", 0);
        total.setText("$" + totalAmount);

        TextView tvTotalAmount = findViewById(R.id.rginpok5lmng);
        TextView tvShipping = findViewById(R.id.rugqtneve0y);
        TextView tvDiscount = findViewById(R.id.tv_discount);
        TextView tvFinalTotal = findViewById(R.id.revrvnohagb9);
        TextView tvTotal = findViewById(R.id.tv_total);

        // 從 Firestore 讀取運費與折扣，再計算總金額
        db.collection("users")
                .document(uid)
                .collection("cart_info")
                .document("summary")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //Long shippingFee = documentSnapshot.getLong("shippingFee");
                        Long discount = documentSnapshot.getLong("discount");

                        //if (shippingFee == null) shippingFee = 0L;
                        if (discount == null) discount = 0L;
                        long shippingFee = 30;
                        tvShipping.setText("$" + shippingFee);
                        tvDiscount.setText("-$" + discount);

                        long finalTotal = totalAmount + shippingFee - discount;
                        long Total = totalAmount + shippingFee - discount;
                        tvFinalTotal.setText("$" + finalTotal);
                        tvTotal.setText("$" + finalTotal);

                    } else {
                        tvShipping.setText("$0");
                        tvDiscount.setText("-$0");
                        tvFinalTotal.setText("$" + totalAmount);
                        tvTotal.setText("$" + totalAmount);

                    }
                });

        EditText etName = findViewById(R.id.et_name);
        EditText etCardNum = findViewById(R.id.et_card_num);
        EditText etDate = findViewById(R.id.et_date);
        EditText etVerificationCode = findViewById(R.id.et_tv_verification_code);
        LinearLayout llCommit = findViewById(R.id.ll_commit);



        llCommit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String cardNum = etCardNum.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String verificationCode = etVerificationCode.getText().toString().trim();

            // 基本欄位空值檢查
            if (name.isEmpty() || cardNum.isEmpty() || date.isEmpty() || verificationCode.isEmpty()) {
                Toast.makeText(CheckoutCart.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 卡號限制：16位且都是數字
            if (cardNum.length() != 16 || !cardNum.matches("\\d{16}")) {
                Toast.makeText(CheckoutCart.this, "卡號必須為16位數字", Toast.LENGTH_SHORT).show();
                return;
            }

            // 日期格式檢查：月份在01~12之間
            String monthStr = date.substring(0, 2);
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                Toast.makeText(CheckoutCart.this, "月份必須在01到12之間", Toast.LENGTH_SHORT).show();
                return;
            }

            if (verificationCode.length() != 3 || !verificationCode.matches("\\d{3}")) {
                Toast.makeText(CheckoutCart.this, "驗證碼必須為3位數字", Toast.LENGTH_SHORT).show();
                return;
            }

            // 所有檢查通過
            Toast.makeText(CheckoutCart.this, "付款成功，謝謝您的訂購！", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CheckoutCart.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }
}