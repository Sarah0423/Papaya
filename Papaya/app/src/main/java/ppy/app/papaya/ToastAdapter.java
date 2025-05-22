package ppy.app.papaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class  ToastAdapter extends RecyclerView.Adapter<ToastAdapter.ToastViewHolder> {

    private Context context;
    private List<ToastItem> toastItemList;

    public ToastAdapter(Context context, List<ToastItem> toastItemList) {
        this.context = context;
        this.toastItemList = toastItemList;
    }

    @NonNull
    @Override
    public ToastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_toast, parent, false);
        return new ToastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToastViewHolder holder, int position) {
        ToastItem toastItem = toastItemList.get(position);

        holder.tvName.setText(toastItem.getName());
        holder.tvPrice.setText("$" + toastItem.getPrice());
        holder.tvInfo.setText(toastItem.getInfo());

        // 使用圖片名稱找 drawable 資源 ID
        int imageResId = context.getResources().getIdentifier(
                toastItem.getImageName(), "drawable", context.getPackageName());

        if (imageResId != 0) {
            holder.ivImage.setImageResource(imageResId);
        } else {
            holder.ivImage.setImageResource(R.mipmap.bigtoast); // 顯示預設圖片
        }
    }

    @Override
    public int getItemCount() {
        return toastItemList.size();
    }

    public static class ToastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvInfo;

        public ToastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_toast_item_img);
            tvName = itemView.findViewById(R.id.tv_toast_item_name);
            tvPrice = itemView.findViewById(R.id.tv_toast_item_price);
            tvInfo = itemView.findViewById(R.id.tv_toast_item_info);
        }
    }
}

