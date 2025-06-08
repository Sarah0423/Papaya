package ppy.app.papaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private Context context;
    private List<CouponInfo> couponList;
    private int selectedPosition = -1; // ⭐ 記錄目前選中的位置

    public CouponAdapter(Context context, List<CouponInfo> couponList) {
        this.context = context;
        this.couponList = couponList;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        CouponInfo coupon = couponList.get(position);
        holder.tvCouponName.setText(coupon.getCouponName());
        holder.tvCouponInfo.setText(coupon.getCouponInfo());

        int imageResId = context.getResources().getIdentifier(
                coupon.getCouponPhoto(), "mipmap", context.getPackageName());
        holder.ivCouponImg.setImageResource(imageResId);

        //  點擊後選中，並改變背景顏色
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            int previous = selectedPosition;
            selectedPosition = adapterPosition;
            if (previous != -1) notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
        });
        // 改變選中時的背景色
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEB3B")); // 黃色表示選中
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // 白色為未選
        }
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    // 對外提供：取得目前選中的 CouponInfo
    public CouponInfo getSelectedCoupon() {
        if (selectedPosition >= 0 && selectedPosition < couponList.size()) {
            return couponList.get(selectedPosition);
        }
        return null;
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCouponImg;
        TextView tvCouponName, tvCouponInfo;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCouponImg = itemView.findViewById(R.id.iv_coupon_img);
            tvCouponName = itemView.findViewById(R.id.tv_coupon_name);
            tvCouponInfo = itemView.findViewById(R.id.tv_coupon_info);
        }
    }
}
