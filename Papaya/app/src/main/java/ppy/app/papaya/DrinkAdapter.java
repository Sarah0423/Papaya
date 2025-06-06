package ppy.app.papaya;

import android.content.Context;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {
    private Context context;
    private List<DrinkItem> drinkList;

    public DrinkAdapter(Context context, List<DrinkItem> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_toast, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        DrinkItem drinkItem = drinkList.get(position);
        holder.tvName.setText(drinkItem.getBeverage_name());
        holder.tvInfo.setText(drinkItem.getBeverage_info() + "\n\n");
        holder.tvPrice.setText("$" + drinkItem.getBeverage_price());

        // 取得 mipmap 中的圖片資源 ID
        int imageResId = context.getResources().getIdentifier(
                drinkItem.getBeverage_photo(),  // 圖片檔名，不包含副檔名
                "mipmap",
                context.getPackageName()
        );

        // 設定圖片
        if (imageResId != 0) {
            holder.ivPhoto.setImageResource(imageResId);
        } else {
            holder.ivPhoto.setImageResource(R.mipmap.bigtoast);
        }
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo, tvPrice;
        ImageView ivPhoto;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_toast_item_name);
            tvInfo = itemView.findViewById(R.id.tv_toast_item_info);
            tvPrice = itemView.findViewById(R.id.tv_toast_item_price);
            ivPhoto = itemView.findViewById(R.id.iv_toast_item_img);
        }
    }
}

