package ppy.app.papaya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class Branch_info extends AppCompatActivity {

    private ImageButton btnReturn;
    private FirebaseFirestore db;
    private ImageButton btnTaipei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_branch_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnReturn= findViewById(R.id.btn_return);
        btnTaipei = findViewById(R.id.btn_taipei);

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Branch_info.this, MainActivity.class);
                intent.putExtra("SHOW_FUNCTION_MENU", true);
                startActivity(intent);

            }
        });

        // 初始化 Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // 設置按鈕點擊監聽器
        View.OnClickListener cityButtonListener = v -> {
            String cityName = (String) v.getTag();
            if (cityName != null) {
                loadCityData(cityName);
            } else {
                Log.e("Branch_info", "Button tag is null");
            }
        };

        int[] buttonIds = {
                R.id.btn_hsinchu,
                R.id.btn_hualian,
                R.id.btn_kaohsiung,
                R.id.btn_nantou,
                R.id.btn_newtaipei,
                R.id.btn_pingtung,
                R.id.btn_taichung,
                R.id.btn_tainan,
                R.id.btn_taipei,
                R.id.btn_taoyuan,
                R.id.btn_yilan,
                R.id.btn_yunlin
        };

        for (int id : buttonIds) {
            View button = findViewById(id);
            if (button != null) {
                button.setOnClickListener(cityButtonListener);
            }
        }
    }

    private void loadCityData(String cityName) {
        db.collection("map").document(cityName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String mapImg = documentSnapshot.getString("map_img");
                        String mapName = documentSnapshot.getString("map_name");
                        String mapTel = documentSnapshot.getString("map_tel");
                        String mapAddress = documentSnapshot.getString("map_address");

                        // 注意這裡 map_point 是一個 GeoPoint 物件
                        GeoPoint mapPoint = documentSnapshot.getGeoPoint("map_point");
                        double latitude = mapPoint.getLatitude();
                        double longitude = mapPoint.getLongitude();

                        // 跳到 LocationInfo，傳資料過去
                        Intent intent = new Intent(Branch_info.this, LocationInfo.class);
                        intent.putExtra("map_img", mapImg);
                        intent.putExtra("map_name", mapName);
                        intent.putExtra("map_tel", mapTel);
                        intent.putExtra("map_address", mapAddress);
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        startActivity(intent);
                    } else {
                        Log.d("Branch_info", "Document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e("Branch_info", "Error fetching document", e));
    }

}