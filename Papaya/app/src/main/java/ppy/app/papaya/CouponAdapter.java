package ppy.app.papaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private Context context;
    private List<CouponInfo> couponList;

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
    }

    @Override
    public int getItemCount() {
        return couponList.size();
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