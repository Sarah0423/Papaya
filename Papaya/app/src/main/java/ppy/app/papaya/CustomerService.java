package ppy.app.papaya;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerService extends AppCompatActivity {

    private static final String[] QUESTION_TYPES = {"商品問題", "付款問題", "運送問題", "應用程式問題", "其他問題"};

    private ImageButton btnReturn;
    private TextView tvServicePhoneNumber;
    private TextView etQuestionType;
    private TextView etDetailInfo;
    private Button btnSubmitForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn= findViewById(R.id.btn_return);
        tvServicePhoneNumber = findViewById(R.id.tv_service_phone_number);
        etQuestionType = findViewById(R.id.et_question_type);
        etDetailInfo = findViewById(R.id.et_detail_info);
        btnSubmitForm = findViewById(R.id.btn_submit_form);

        tvServicePhoneNumber.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerService.this, MainActivity.class);
                intent.putExtra("SHOW_FUNCTION_MENU", true);
                startActivity(intent);

            }
        });

        etQuestionType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuestionTypeDialog();
            }
        });

        btnSubmitForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFormToFirebase();
            }
        });

        tvServicePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serviceNumber = tvServicePhoneNumber.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + serviceNumber));
                startActivity(intent);
            }
        });
    }

    private int selectedQuestionIndex = -1; // 預設未選擇

    private void showQuestionTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("選擇問題類型")
                .setItems(QUESTION_TYPES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedQuestionIndex = which; // 記錄選擇的數字
                        String selectedType = QUESTION_TYPES[which];
                        etQuestionType.setText(selectedType);
                    }
                });
        builder.show();
    }

    private void submitFormToFirebase() {
        String detailInfo = etDetailInfo.getText().toString();

        if (selectedQuestionIndex == -1 || detailInfo.isEmpty()) {
            Toast.makeText(this, "請選擇問題類型並填寫詳細資訊", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        String userUid = (currentUser != null) ? currentUser.getUid() : "unknown";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> formData = new HashMap<>();
        formData.put("form_type", selectedQuestionIndex); // 傳數字
        formData.put("form_detail", detailInfo);
        formData.put("form_user_id", userUid);

        db.collection("forms")
                .add(formData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(CustomerService.this, "表單提交成功", Toast.LENGTH_SHORT).show();
                        etQuestionType.setText("");
                        selectedQuestionIndex = -1;
                        etDetailInfo.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CustomerService.this, "表單提交失敗", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}