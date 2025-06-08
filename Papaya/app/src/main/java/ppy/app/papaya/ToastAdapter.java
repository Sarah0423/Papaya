package ppy.app.papaya;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class  ToastAdapter extends RecyclerView.Adapter<ToastAdapter.ToastViewHolder> {

    private Context context;
    private FirebaseAuth mAuth;
    private List<ToastItem> toastItemList;

    public ToastAdapter(Context context, List<ToastItem> toastItemList) {
        this.context = context;
        this.toastItemList = toastItemList;
        this.mAuth = FirebaseAuth.getInstance();
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
        holder.tvName.setText(toastItem.getToastName());
        holder.tvPrice.setText("$" + toastItem.getToastPrice());
        holder.tvInfo.setText(toastItem.getToastInfo());

        // 使用圖片名稱找 drawable 資源 ID
        int imageResId = context.getResources().getIdentifier(
                toastItem.getToastImageName(), "mipmap", context.getPackageName());

        if (imageResId != 0) {
            holder.ivImage.setImageResource(imageResId);
        } else {
            holder.ivImage.setImageResource(R.mipmap.bigtoast); // 顯示預設圖片
        }

        holder.btnGotoInfo.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(context, "請先登入帳號", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, Login.class);
                context.startActivity(intent);
            }else {
                Intent intent = new Intent(context, ToastInfo.class);
                intent.putExtra("toast_index", toastItem.getToastIndex());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return toastItemList.size();
    }

    public static class ToastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvInfo;
        Button btnGotoInfo;

        public ToastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_toast_item_img);
            tvName = itemView.findViewById(R.id.tv_toast_item_name);
            tvPrice = itemView.findViewById(R.id.tv_toast_item_price);
            tvInfo = itemView.findViewById(R.id.tv_toast_item_info);
            btnGotoInfo = itemView.findViewById(R.id.btn_goto_toast_info);
        }
    }


}

