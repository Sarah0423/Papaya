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

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> itemList;

    public CartItemAdapter(Context context, List<CartItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_in_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        int resId = context.getResources().getIdentifier(item.getItem_photo(), "mipmap", context.getPackageName());
        holder.ivItemPhoto.setImageResource(resId);

        holder.tvItemName.setText(item.getItem_name());
        holder.tvIngredients.setText(item.getItem_selected());
        holder.tvQuantity.setText(String.valueOf(item.getItem_quantity()));
        holder.tvPrice.setText("$" + item.getItem_price());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemPhoto;
        TextView tvItemName, tvIngredients, tvQuantity, tvPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemPhoto = itemView.findViewById(R.id.iv_item_in_cart);
            tvItemName = itemView.findViewById(R.id.tv_item_in_cart);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients_in_cart);
            tvQuantity = itemView.findViewById(R.id.tv_num_in_cart);
            tvPrice = itemView.findViewById(R.id.tv_price_in_cart);
        }
    }
}
