package ppy.app.papaya;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationInfo extends AppCompatActivity  implements OnMapReadyCallback {

    private ImageButton btnReturn;
    private MapView mapView;
    private GoogleMap gMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyDbXIcgrNpplICHpMYg2XfDPDuNeD6A1YY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.location_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String mapImg = intent.getStringExtra("map_img");
        String mapName = intent.getStringExtra("map_name");
        String mapTel = intent.getStringExtra("map_tel");
        String mapAddress = intent.getStringExtra("map_address");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        FrameLayout frameLayout = findViewById(R.id.frameLayout);
        View branchView = getLayoutInflater().inflate(R.layout.branches, frameLayout, false);
        frameLayout.addView(branchView);

        btnReturn= findViewById(R.id.btn_return);

        // 設定圖片與文字
        ImageView ivBranchInfoImg = branchView.findViewById(R.id.iv_branch_info_img);
        TextView tvBranchInfoName = branchView.findViewById(R.id.tv_branch_info_name);
        TextView tvBranchInfoTel = branchView.findViewById(R.id.tv_branch_info_tel);
        TextView tvBranchInfoAddress = branchView.findViewById(R.id.tv_branch_info_address);

        int imgResId = getResources().getIdentifier(mapImg, "mipmap", getPackageName());
        ivBranchInfoImg.setImageResource(imgResId);
        tvBranchInfoName.setText(mapName);
        tvBranchInfoTel.setText(mapTel);
        tvBranchInfoTel.setPaintFlags(tvBranchInfoTel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvBranchInfoAddress.setText(mapAddress);

        // 初始化地圖
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.TPmapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // 存入座標供 onMapReady 使用
        this.latitude = latitude;
        this.longitude = longitude;

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationInfo.this, Branch_info.class);
                startActivity(intent);

            }
        });

        tvBranchInfoTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清理號碼格式，例如去掉括號與空白
                String cleanNumber = mapTel.replaceAll("[^\\d+]", "");
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cleanNumber));
                startActivity(intent);
            }
        });
    }

    private double latitude;
    private double longitude;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        LatLng branchLocation = new LatLng(latitude, longitude);
        Intent intent = getIntent();
        String mapName = intent.getStringExtra("map_name");;
        gMap.addMarker(new MarkerOptions().position(branchLocation).title("Papaya "+mapName));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(branchLocation, 18));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}