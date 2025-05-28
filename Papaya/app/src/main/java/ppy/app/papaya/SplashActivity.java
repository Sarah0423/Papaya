package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashImageView;
    private int currentFrame = 0;
    private Handler handler = new Handler();
    private int[] splashFrames = {
            R.drawable.splash_1, R.drawable.splash_2, R.drawable.splash_2,
            R.drawable.splash_3, R.drawable.splash_4, R.drawable.splash_5,
            R.drawable.splash_6, R.drawable.splash_7, R.drawable.splash_8,
            R.drawable.splash_9, R.drawable.splash_10, R.drawable.splash_11
    };
    private static final int FRAME_DURATION = 200; // 每張200ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashImageView = findViewById(R.id.splash_image);

        // 啟動動畫
        handler.post(frameRunnable);
    }

    private Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentFrame < splashFrames.length) {
                splashImageView.setImageResource(splashFrames[currentFrame]);
                currentFrame++;
                handler.postDelayed(this, FRAME_DURATION);
            } else {
                // 動畫結束，進入主畫面
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };
}
