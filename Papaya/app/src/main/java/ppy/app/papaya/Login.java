package ppy.app.papaya;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class Login extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private ImageButton btnReturn;
    private FirebaseAuth mAuth;
    private TextView tvForget_Password;
    private TextView tvLoginSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnReturn = findViewById(R.id.btn_return);

        tvForget_Password = findViewById(R.id.tv_forget_password);
        tvLoginSignin = findViewById(R.id.tv_login_signin);

        tvForget_Password.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvLoginSignin.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        tvForget_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 建立 AlertDialog 輸入框
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("重設密碼");

                final EditText input = new EditText(Login.this);
                input.setHint("請輸入您的電子郵件");
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);

                // 設定「送出」按鈕
                builder.setPositiveButton("送出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = input.getText().toString().trim();
                        if (email.isEmpty()) {
                            Toast.makeText(Login.this, "請輸入電子郵件地址", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Login.this, "已寄出重設密碼信件，請查看您的信箱", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(Login.this, "發送失敗，請確認信箱是否正確", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                });

                // 設定「取消」按鈕
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // 顯示對話框
                builder.show();
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userId = user != null ? user.getUid() : "guest";

                                    SharedPreferences preferences = Login.this.getSharedPreferences("ingredient_prefs_" + userId, Context.MODE_PRIVATE);
                                    preferences.edit().clear().apply();

                                    SharedPreferences toastPrefs = Login.this.getSharedPreferences("ToastSelections_" + userId, Context.MODE_PRIVATE);
                                    toastPrefs.edit().clear().apply();

                                    Toast.makeText(Login.this, "登入成功：" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "登入失敗：" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("SHOW_FUNCTION_MENU", true);
                startActivity(intent);

            }
        });


        TextView btnLogSignin = findViewById(R.id.tv_login_signin);
        btnLogSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);

                startActivity(intent);

            }
        });


    }
}