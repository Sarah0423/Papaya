package ppy.app.papaya;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DeliveryStatus extends AppCompatActivity {

    private TextView etaText;
    private ProgressBar progressBar;

    private static final int TOTAL_TIME = 5 * 60 * 1000; // 5 分鐘
    private static final int INTERVAL = 1000; // 每秒更新一次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        etaText = findViewById(R.id.etaText);
        progressBar = findViewById(R.id.progressBar);

        startCountdown();
    }

    private void startCountdown() {
        new CountDownTimer(TOTAL_TIME, INTERVAL) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                etaText.setText("預計送達時間：" + minutes + " 分 " + seconds + " 秒");

                int progress = (int) (millisUntilFinished * 100 / TOTAL_TIME);
                progressBar.setProgress(progress);
            }

            public void onFinish() {
                etaText.setText("外送已送達！");
                progressBar.setProgress(0);
            }
        }.start();
    }
}