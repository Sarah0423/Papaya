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
        ImageView ivIsGet = holder.itemView.findViewById(R.id.iv_isget);
        TextView tvOrderDetail = holder.itemView.findViewById(R.id.tv_order_detail);
        OrderItem item = orderList.get(position);
        long currentTimeMillis = System.currentTimeMillis();
        long orderTimeMillis = item.getOrderTimeMillis();
        String address = item.getAddress();
        String orderId = item.getOrderId();
        String shortOrderId = orderId.length() > 12 ? orderId.substring(0, 12) + "..." : orderId;
        holder.tvOrderName.setText(shortOrderId);

        holder.tvOrderPrice.setText("$" + item.getFinalPrice());

        if (address != null && address.contains("自取")) {
            ivIsGet.setImageResource(R.mipmap.order_pickup);
        } else if (currentTimeMillis - orderTimeMillis >= 5 * 60 * 1000) { // 超過 5 分鐘
            ivIsGet.setImageResource(R.mipmap.order_arrive);
        } else {
            ivIsGet.setImageResource(R.mipmap.order_delivering); // 預設
        }

        if (address != null && address.contains("自取")) {
            tvOrderDetail.setText("自取");
        } else if (currentTimeMillis - orderTimeMillis >= 5 * 60 * 1000) {
            tvOrderDetail.setText("已送達");
        } else {
            tvOrderDetail.setText("運送中");
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
