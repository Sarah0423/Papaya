package ppy.app.papaya;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<IngredientItem> ingredientList;
    private Context context;
    private OnIngredientSelectedListener listener;

    public List<IngredientItem> getIngredientList() {
        return ingredientList;
    }

    public IngredientAdapter(List<IngredientItem> ingredientList, Context context, OnIngredientSelectedListener listener) {
        this.ingredientList = ingredientList;
        this.context = context;
        this.listener = listener;
    }

    public interface OnIngredientSelectedListener {
        void onIngredientSelected();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.button_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientItem item = ingredientList.get(position);
        String displayText = item.getIngredientsName() + "+$" + item.getIngredientsPrice();
        holder.btnIngredient.setText(displayText);

        // 初始化顏色狀態
        updateButtonState(holder.btnIngredient, item.isSelected());

        holder.btnIngredient.setOnClickListener(v -> {
            String type = item.getIngredientsType();

            if (!item.isSelected()) {
                if (type.equals("meat") || type.equals("fruit")) {
                    for (IngredientItem i : ingredientList) {
                        if ((i.getIngredientsType().equals("meat") || i.getIngredientsType().equals("fruit")) && i.isSelected()) {
                            i.setSelected(false);
                        }
                    }
                    item.setSelected(true);
                } else if (ingredientList.stream().filter(IngredientItem::isSelected).count() < 2) {
                    item.setSelected(true);
                } else {
                    Toast.makeText(context, "最多選擇兩個", Toast.LENGTH_SHORT).show();
                }
            } else {
                item.setSelected(false);
            }

            // 更新所有按鈕顏色
            notifyDataSetChanged();
            listener.onIngredientSelected();
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btnIngredient;
        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            btnIngredient = itemView.findViewById(R.id.btn_ingredient);
        }
    }

    private void updateButtonState(MaterialButton button, boolean isSelected) {
        button.setTextColor(isSelected ?
                ContextCompat.getColor(context, R.color.orange) :
                ContextCompat.getColor(context, R.color.beige));

        button.setStrokeColor(
                ColorStateList.valueOf(
                        isSelected ?
                                ContextCompat.getColor(context, R.color.orange) :
                                ContextCompat.getColor(context, R.color.beige)
                )
        );
        button.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor("#00FFFFFF")));
    }
}
