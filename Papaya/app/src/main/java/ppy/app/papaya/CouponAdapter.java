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

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private Context context;
    private List<CouponInfo> couponList;
    private Map<String, CouponInfo> selectedCouponsByType = new HashMap<>();

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

        Timestamp expireAtTimestamp = coupon.getExpireAt();
        TextView tvRemainingTime = holder.itemView.findViewById(R.id.tv_coupon_remaining_time);

        int imageResId = context.getResources().getIdentifier(
                coupon.getCouponPhoto(), "mipmap", context.getPackageName());
        holder.ivCouponImg.setImageResource(imageResId);

        if (expireAtTimestamp == null) {
            tvRemainingTime.setText("無效時間");
            holder.itemView.setAlpha(0.5f); // 灰色
        } else {
            long now = System.currentTimeMillis();
            long expireAt = expireAtTimestamp.toDate().getTime();
            long diffMillis = expireAt - now;

            if (diffMillis <= 0) {
                tvRemainingTime.setText("已過期");
                holder.itemView.setAlpha(0.5f);
            } else {
                long minutes = diffMillis / (60 * 1000);
                if (minutes < 60) {
                    tvRemainingTime.setText("剩餘 " + minutes + " 分鐘");
                } else {
                    long hours = minutes / 60;
                    tvRemainingTime.setText("剩餘 " + hours + " 小時");
                }
                holder.itemView.setAlpha(1f);
            }
        }
        // 設定點擊行為
        holder.itemView.setOnClickListener(v -> {
            String type = coupon.getCouponType();
            String key = type.equals("discount_delivery") ? "delivery" : "other";

            if (selectedCouponsByType.containsKey(key) && selectedCouponsByType.get(key).equals(coupon)) {
                selectedCouponsByType.remove(key); // 再次點擊 → 取消選取
            } else {
                selectedCouponsByType.put(key, coupon); // 選中這張券，取代舊的同類型
            }
            notifyDataSetChanged(); // 更新整個列表背景
        });

        // 決定是否被選中
        boolean isSelected = selectedCouponsByType.containsValue(coupon);
        holder.itemView.setBackgroundColor(isSelected ? Color.parseColor("#FFEB3B") : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public List<CouponInfo> getSelectedCoupons() {
        return new ArrayList<>(selectedCouponsByType.values());
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