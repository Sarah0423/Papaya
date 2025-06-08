package ppy.app.papaya;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderItem> orderList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OrderItem order);
    }

    public OrderAdapter(Context context, List<OrderItem> orderList, OnItemClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = orderList.get(position);
        holder.tvOrderName.setText(item.getOrderId());
        holder.tvOrderPrice.setText("$" + item.getFinalPrice());

        int resId = context.getResources().getIdentifier(item.getMapImageName(), "mipmap", context.getPackageName());
        if (resId != 0) {
            holder.ivOrder.setImageResource(resId);
        } else {
            holder.ivOrder.setImageResource(R.mipmap.bigtoast); // 預設圖片
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrder;
        TextView tvOrderName, tvOrderPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrder = itemView.findViewById(R.id.iv_order);
            tvOrderName = itemView.findViewById(R.id.tv_order_name);
            tvOrderPrice = itemView.findViewById(R.id.tv_order_price);
        }
    }
}
