package ppy.app.papaya;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private ImageView ivUserRegisterImg;
    private String selectedAvatarName = "login_usagi"; // 預設值


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        editName = findViewById(R.id.editTextName);
        editEmail = findViewById(R.id.et_login_email);
        editPassword = findViewById(R.id.editTextPassword);
        btnRegister = findViewById(R.id.btn_register);
        editConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        ivUserRegisterImg = findViewById(R.id.iv_user_register_img);
        ImageButton ibChooseUserImg = findViewById(R.id.ib_choose_user_img);

        ibChooseUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChooseAvatarDialog();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String confirm_password = editConfirmPassword.getText().toString().trim();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (name.trim().isEmpty()){
                            Toast.makeText(Register.this, "請輸入使用者名稱", Toast.LENGTH_LONG).show();
                        }
                        else if (!password.equals(confirm_password)){
                            Toast.makeText(Register.this, "密碼與確認密碼不符", Toast.LENGTH_LONG).show();
                        }
                        else if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();
                            String name = editName.getText().toString().trim();
                            String email = user.getEmail();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("name", name);
                            userInfo.put("email", email);
                            userInfo.put("avatar", selectedAvatarName);

                            db.collection("users").document(uid)
                                    .set(userInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Register.this, "使用者資料已儲存", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, Login.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "資料儲存失敗：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });


                            Toast.makeText(Register.this, "註冊成功：" + user.getEmail(), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Register.this, Login.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Register.this, "註冊失敗：" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
    private void openChooseAvatarDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.choose_user_img);

        ImageButton ibUnknown = dialog.findViewById(R.id.ib_unknown_user_img);
        ImageButton ibChii = dialog.findViewById(R.id.ib_chii_user_img);
        ImageButton ibHachi = dialog.findViewById(R.id.ib_hachi_user_img);
        ImageButton ibUsagi = dialog.findViewById(R.id.ib_usagi_user_img);

        ibUnknown.setOnClickListener(v -> {
            selectedAvatarName = "login_usagi";
            ivUserRegisterImg.setImageResource(R.mipmap.login_usagi);
            dialog.dismiss();
        });

        ibChii.setOnClickListener(v -> {
            selectedAvatarName = "user_chii";
            ivUserRegisterImg.setImageResource(R.mipmap.user_chii);
            dialog.dismiss();
        });

        ibHachi.setOnClickListener(v -> {
            selectedAvatarName = "user_hachi";
            ivUserRegisterImg.setImageResource(R.mipmap.user_hachi);
            dialog.dismiss();
        });

        ibUsagi.setOnClickListener(v -> {
            selectedAvatarName = "user_usagi";
            ivUserRegisterImg.setImageResource(R.mipmap.user_usagi);
            dialog.dismiss();
        });

        dialog.show();
    }
}