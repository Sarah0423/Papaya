package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DailySpinActivity extends AppCompatActivity {

    private ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_spin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View.OnClickListener prizeClickListener = v -> {
            int random = (int) (Math.random() * 9) + 1;  // 1 ~ 9
            int couponIndex = 0;
            String prizeMessage;

            switch (random) {
                case 1:
                    couponIndex = 1;
                    prizeMessage = "獲得免外送費";
                    break;
                case 2:
                    couponIndex = 2;
                    prizeMessage = "獲得$30折價券";
                    break;
                case 3:
                    couponIndex = 3;
                    prizeMessage = "吐司買一送一";
                    break;
                case 4:
                    couponIndex = 4;
                    prizeMessage = "獲得$60折價券";
                    break;
                case 5:
                    couponIndex = 5;
                    prizeMessage = "滿百享九折";
                    break;
                case 6:
                    couponIndex = 6;
                    prizeMessage = "吐司買二送一";
                    break;
                default:
                    prizeMessage = "沒中獎，祝下次好運！";
                    showResultDialog(prizeMessage);
                    return;
            }

            // 抽中優惠券：寫入 Firestore
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // ===== 寫入 owned_coupons =====
                Map<String, Object> couponData = new HashMap<>();
                couponData.put("coupon_index", couponIndex);
                couponData.put("acquired_at", Timestamp.now());

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, 1);
                couponData.put("expire_at", new Timestamp(cal.getTime()));
                couponData.put("is_used", false);

                db.collection("users")
                        .document(uid)
                        .collection("owned_coupons")
                        .add(couponData)
                        .addOnSuccessListener(docRef -> {
                            String sameId = docRef.getId();  // 取得相同的 document ID

                            // ===== 寫入 alarms =====
                            Map<String, Object> alarmData = new HashMap<>();
                            alarmData.put("alarms_info", prizeMessage);
                            alarmData.put("alarms_photo", "egg_coupon");
                            alarmData.put("alarms_time", Timestamp.now());
                            alarmData.put("alarms_type", "獲得優惠券");

                            db.collection("users")
                                    .document(uid)
                                    .collection("alarms")
                                    .document(sameId)  // 用相同 ID
                                    .set(alarmData)
                                    .addOnSuccessListener(alarmDocRef -> {
                                        showResultDialog(prizeMessage + "\n⚠️ 請24小時內使用");
                                    })
                                    .addOnFailureListener(e -> {
                                        showErrorDialog("警報儲存失敗，請稍後再試");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            showErrorDialog("優惠券儲存失敗，請稍後再試");
                        });

            } else {
                showErrorDialog("請先登入帳號以使用抽獎功能");
            }
        };

        // 為所有角色圖片設定點擊事件
        int[] ids = {
                R.id.btn_chii, R.id.btn_shisa, R.id.btn_rakko, R.id.btn_kuri,
                R.id.btn_bug, R.id.btn_momo, R.id.btn_kani, R.id.btn_hachi,
                R.id.btn_usa
        };
        for (int id : ids) {
            findViewById(id).setOnClickListener(prizeClickListener);
        }

        // 返回主畫面
        btnReturn = findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(DailySpinActivity.this, MainActivity.class);
            intent.putExtra("SHOW_FUNCTION_MENU", true);
            startActivity(intent);
            finish();
        });
    }

    private void showResultDialog(String message) {
        new AlertDialog.Builder(DailySpinActivity.this)
                .setTitle("抽獎結果")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("確定", (dialog, which) -> {
                    Intent intent = new Intent(DailySpinActivity.this, MainActivity.class);
                    intent.putExtra("SHOW_FUNCTION_MENU", false);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showErrorDialog(String error) {
        new AlertDialog.Builder(DailySpinActivity.this)
                .setTitle("錯誤")
                .setMessage(error)
                .setPositiveButton("確定", null)
                .show();
    }
}



