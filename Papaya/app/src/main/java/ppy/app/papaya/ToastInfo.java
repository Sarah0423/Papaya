package ppy.app.papaya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToastInfo extends AppCompatActivity {

    private ImageButton btnReturn;
    private ImageButton ibCurToast, ibPreToast, ibNextToast, ibMinusNum, ibPlusNum;
    private TextView tvToastIntro, tvToastNameCur, tvToastPrice, tvToastNum;
    private FirebaseFirestore db;
    private List<ToastItem> toastList = new ArrayList<>();
    private int currentIndex;
    private int extraTotalPrice = 0;
    private List<IngredientItem> allSelectedIngredients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_toast_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        btnReturn = findViewById(R.id.btn_return);
        ibCurToast = findViewById(R.id.ib_cur_toast);
        ibPreToast = findViewById(R.id.ib_pre_toast);
        ibNextToast = findViewById(R.id.ib_next_toast);
        tvToastIntro = findViewById(R.id.tv_toast_intro);
        tvToastNameCur = findViewById(R.id.tv_toast_name_cur);
        tvToastPrice = findViewById(R.id.tv_toast_price);
        tvToastNum = findViewById(R.id.tv_toast_num);

        rvMeat = findViewById(R.id.rv_meat);
        rvVegetable = findViewById(R.id.rv_vegetable);
        rvFruit = findViewById(R.id.rv_fruit);
        rvCommon = findViewById(R.id.rv_common);
        rvJam = findViewById(R.id.rv_jam);

        ibMinusNum = findViewById(R.id.ib_minus_num);
        ibPlusNum = findViewById(R.id.ib_plus_num);

        GridLayoutManager gridLayout = new GridLayoutManager(this, 3);
        rvMeat.setLayoutManager(new GridLayoutManager(this, 3));
        rvVegetable.setLayoutManager(new GridLayoutManager(this, 3));
        rvFruit.setLayoutManager(new GridLayoutManager(this, 3));
        rvCommon.setLayoutManager(new GridLayoutManager(this, 3));
        rvJam.setLayoutManager(new GridLayoutManager(this, 3));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        rvMeat.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        rvVegetable.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        rvFruit.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        rvCommon.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        rvJam.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        List<IngredientItem> meatList = new ArrayList<>();
        List<IngredientItem> vegetableList = new ArrayList<>();
        List<IngredientItem> fruitList = new ArrayList<>();
        List<IngredientItem> commonList = new ArrayList<>();
        List<IngredientItem> jamList = new ArrayList<>();

        meatList.clear();
        vegetableList.clear();
        fruitList.clear();
        commonList.clear();
        jamList.clear();

        db.collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        IngredientItem item = doc.toObject(IngredientItem.class);
                        switch (item.getIngredientsType()) {
                            case "meat":
                                meatList.add(item);
                                break;
                            case "vegetable":
                                vegetableList.add(item);
                                break;
                            case "fruit":
                                fruitList.add(item);
                                break;
                            case "common":
                                commonList.add(item);
                                break;
                            case "jam":
                                jamList.add(item);
                                break;
                        }
                    }

                    rvMeat.setAdapter(new IngredientAdapter(meatList, this, this::updateExtraPrice));
                    rvVegetable.setAdapter(new IngredientAdapter(vegetableList, this, this::updateExtraPrice));
                    rvFruit.setAdapter(new IngredientAdapter(fruitList, this, this::updateExtraPrice));
                    rvCommon.setAdapter(new IngredientAdapter(commonList, this, this::updateExtraPrice));
                    rvJam.setAdapter(new IngredientAdapter(jamList, this, this::updateExtraPrice));

                });

        int toastIndex = getIntent().getIntExtra("toast_index", 0);

        db.collection("toast")
                .orderBy("toast_index")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ToastItem item = doc.toObject(ToastItem.class);
                        toastList.add(item);
                    }

                    // 找到當前 index 對應位置
                    for (int i = 0; i < toastList.size(); i++) {
                        if (toastList.get(i).getToastIndex() == toastIndex) {
                            currentIndex = i;
                            break;
                        }
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "資料讀取失敗", Toast.LENGTH_SHORT).show();
                });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ToastInfo.this, MainActivity.class);
                startActivity(intent);

            }
        });

        ibPreToast.setOnClickListener(v -> {
            if (!toastList.isEmpty()) {
                currentIndex = (currentIndex - 1 + toastList.size()) % toastList.size();
                updateUI();
            }
        });

        ibNextToast.setOnClickListener(v -> {
            if (!toastList.isEmpty()) {
                currentIndex = (currentIndex + 1) % toastList.size();
                updateUI();
            }
        });

        tvToastNum.setText("1");

        ibPlusNum.setOnClickListener(v -> {
            int num = Integer.parseInt(tvToastNum.getText().toString());
            num++;
            tvToastNum.setText(String.valueOf(num));
            updateTotalPrice(num);
        });

        ibMinusNum.setOnClickListener(v -> {
            int num = Integer.parseInt(tvToastNum.getText().toString());
            if (num > 1) {
                num--;
                tvToastNum.setText(String.valueOf(num));
                updateTotalPrice(num);
            } else {
                Toast.makeText(this, "數量不能小於 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetExtras() {
        allSelectedIngredients.clear();
        extraTotalPrice = 0;

        for (RecyclerView rv : Arrays.asList(rvMeat, rvVegetable, rvFruit, rvCommon, rvJam)) {
            IngredientAdapter adapter = (IngredientAdapter) rv.getAdapter();
            if (adapter != null) {
                for (IngredientItem item : adapter.getIngredientList()) {
                    item.setSelected(false);  // 取消勾選
                }
                adapter.notifyDataSetChanged();  // 通知刷新畫面
            }
        }
    }


    private void updateUI() {
        if (toastList.isEmpty()) return;

        ToastItem curItem = toastList.get(currentIndex);
        ToastItem preItem = toastList.get((currentIndex - 1 + toastList.size()) % toastList.size());
        ToastItem nextItem = toastList.get((currentIndex + 1) % toastList.size());

        int duration = 200;

        ibCurToast.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            int resId = getResources().getIdentifier(
                    curItem.getToastImageName(), "mipmap", getPackageName());
            ibCurToast.setImageResource(resId);
            ibCurToast.animate().alpha(1f).setDuration(duration);
        }).start();

        ibPreToast.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            int preResId = getResources().getIdentifier(
                    preItem.getToastImageName(), "mipmap", getPackageName());
            ibPreToast.setImageResource(preResId);
            ibPreToast.animate().alpha(1f).setDuration(duration);
        }).start();

        ibNextToast.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            int nextResId = getResources().getIdentifier(
                    nextItem.getToastImageName(), "mipmap", getPackageName());
            ibNextToast.setImageResource(nextResId);
            ibNextToast.animate().alpha(1f).setDuration(duration);
        }).start();

        tvToastNameCur.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            tvToastNameCur.setText(curItem.getToastName());
            tvToastNameCur.animate().alpha(1f).setDuration(duration);
        }).start();

        tvToastIntro.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            tvToastIntro.setText(curItem.getToastInfo());
            tvToastIntro.animate().alpha(1f).setDuration(duration);
        }).start();

        // 重設數量為 1
        tvToastNum.setText("1");

        // 重設配料選擇
        resetExtras();

        restoreSelectedIngredientsForCurrentToast();
        updateExtraPrice(); // 重新計算價格
        updateTotalPrice(1);
    }


    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = space / 2;
            outRect.right = space / 2;
            outRect.top = space / 2;
            outRect.bottom = space / 2;
        }
    }

    private RecyclerView rvMeat, rvVegetable, rvFruit, rvCommon, rvJam;

    private void updateExtraPrice() {
        int total = 0;
        allSelectedIngredients.clear();
        for (RecyclerView rv : Arrays.asList(rvMeat, rvVegetable, rvFruit, rvCommon, rvJam)) {
            IngredientAdapter adapter = (IngredientAdapter) rv.getAdapter();
            if (adapter != null) {
                for (IngredientItem item : adapter.getIngredientList()) {
                    if (item.isSelected()) {
                        allSelectedIngredients.add(item);
                        total += item.getIngredientsPrice();
                    }
                }
            }
        }
        extraTotalPrice = total;
        tvToastPrice.setText("$" + (toastList.get(currentIndex).getToastPrice() + extraTotalPrice));
        saveSelectedIngredientsForCurrentToast();
    }

    private void updateTotalPrice(int num) {
        int basePrice = toastList.get(currentIndex).getToastPrice();
        int totalPrice = (basePrice + extraTotalPrice) * num;
        int duration = 200; // 動畫時間
        tvToastPrice.animate().alpha(0f).setDuration(duration).withEndAction(() -> {
            tvToastPrice.setText("$" + totalPrice);
            tvToastPrice.animate().alpha(1f).setDuration(duration);
        }).start();
    }

    private void saveSelectedIngredientsForCurrentToast() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : "guest";

        SharedPreferences prefs = getSharedPreferences("ToastSelections_" + userId, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> selectedNames = new HashSet<>();
        for (IngredientItem item : allSelectedIngredients) {
            selectedNames.add(item.getIngredientsName());
        }

        String key = "toast_selected_" + toastList.get(currentIndex).getToastIndex();
        editor.putStringSet(key, selectedNames);
        editor.apply();
    }

    private void restoreSelectedIngredientsForCurrentToast() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : "guest";

        SharedPreferences prefs = getSharedPreferences("ToastSelections_" + userId, MODE_PRIVATE);
        String key = "toast_selected_" + toastList.get(currentIndex).getToastIndex();
        Set<String> savedNames = prefs.getStringSet(key, new HashSet<>());

        for (RecyclerView rv : Arrays.asList(rvMeat, rvVegetable, rvFruit, rvCommon, rvJam)) {
            IngredientAdapter adapter = (IngredientAdapter) rv.getAdapter();
            if (adapter != null) {
                for (IngredientItem item : adapter.getIngredientList()) {
                    item.setSelected(savedNames.contains(item.getIngredientsName()));
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}