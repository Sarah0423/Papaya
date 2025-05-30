package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DailySpin extends AppCompatActivity {

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

        View.OnClickListener prizeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = (int) (Math.random() * 9) + 1;
                String prizeMessage;

                switch (random) {
                    case 1:
                        prizeMessage = "獲得免外送費";
                        break;
                    case 2:
                        prizeMessage = "獲得$30折價券";
                        break;
                    case 3:
                        prizeMessage = "吐司買一送一";
                        break;
                    case 4:
                        prizeMessage = "獲得會員點數2點";
                        break;
                    case 5:
                        prizeMessage = "滿百享九折";
                        break;
                    case 6:
                        prizeMessage = "吐司買二送一";
                        break;
                    default:
                        prizeMessage = "沒中獎，再試一次！";
                        break;
                }

                // 儲存中獎結果到 SharedPreferences（推薦方法）
                getSharedPreferences("PapayaPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("lastPrize", prizeMessage)
                        .apply();

                // 顯示中獎結果 Dialog
                new androidx.appcompat.app.AlertDialog.Builder(DailySpin.this)
                        .setTitle("抽獎結果")
                        .setMessage(prizeMessage)
                        .setCancelable(false)
                        .setPositiveButton("確定", (dialog, which) -> {
                            // 點確定後跳轉回 MainActivity
                            Intent intent = new Intent(DailySpin.this, MainActivity.class);
                            intent.putExtra("SHOW_FUNCTION_MENU", false);
                            startActivity(intent);
                            finish(); // 關閉當前頁面
                        })
                        .show();
            }
        };

        int[] ids = {R.id.btn_chii, R.id.btn_shisa, R.id.btn_rakko, R.id.btn_kuri,
                R.id.btn_bug, R.id.btn_momo, R.id.btn_kani, R.id.btn_hachi,
                R.id.btn_usa};

        for (int id : ids) {
            findViewById(id).setOnClickListener(prizeClickListener);
        }


        btnReturn= findViewById(R.id.btn_return);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DailySpin.this, MainActivity.class);
                intent.putExtra("SHOW_FUNCTION_MENU", true);
                startActivity(intent);

            }
        });

    }
}