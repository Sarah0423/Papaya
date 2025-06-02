package ppy.app.papaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> itemList;
    private TextView tvTotal;

    public CartItemAdapter(Context context, List<CartItem> itemList, TextView tvTotal) {
        this.context = context;
        this.itemList = itemList;
        this.tvTotal = tvTotal;
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

        // 🗑️ 刪除按鈕點擊事件
        holder.ibTrash.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) return;

            String uid = currentUser.getUid();

            String docId = item.getFirestoreId();

            db.collection("cart_item").document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 🧮 更新 cart_info
                        int price = item.getItem_price() * item.getItem_quantity();


                        // ❌ 從列表移除
                        int removeIndex = holder.getAdapterPosition();
                        itemList.remove(removeIndex);
                        notifyItemRemoved(removeIndex);

                        Toast.makeText(context, "已移除項目", Toast.LENGTH_SHORT).show();

                    });

        });

        holder.ibMinus.setOnClickListener(v -> {
            if (item.getItem_quantity() > 1) {
                int oldQuantity = item.getItem_quantity();
                int newQuantity = oldQuantity - 1;

                item.setItem_quantity(newQuantity);
                holder.tvQuantity.setText(String.valueOf(newQuantity));

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) return;
                String uid = currentUser.getUid();

                // 更新 cart_item 的數量
                db.collection("cart_item").document(item.getFirestoreId())
                        .update("item_quantity", newQuantity);

                // 更新畫面總價
                updateTotalPrice(tvTotal, itemList);
            } else {
                Toast.makeText(context, "數量不能小於 1", Toast.LENGTH_SHORT).show();
            }
        });

        holder.ibPlus.setOnClickListener(v -> {
            int oldQuantity = item.getItem_quantity();
            int newQuantity = oldQuantity + 1;

            item.setItem_quantity(newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;
            String uid = currentUser.getUid();

            // 更新 cart_item 的數量
            db.collection("cart_item").document(item.getFirestoreId())
                    .update("item_quantity", newQuantity);


            // 更新畫面總價
            updateTotalPrice(tvTotal, itemList);
        });


        holder.tvItemName.setText(item.getItem_name());
        holder.tvIngredients.setText(item.getItem_selected());
        holder.tvIngredients.setVisibility(View.GONE); // 預設隱藏

        holder.ibShowIngredients.setOnClickListener(v -> {
            if (holder.tvIngredients.getVisibility() == View.GONE) {
                holder.tvIngredients.setVisibility(View.VISIBLE);
                holder.ibShowIngredients.setImageResource(R.drawable.ic_collapse); // 換收合圖示
            } else {
                holder.tvIngredients.setVisibility(View.GONE);
                holder.ibShowIngredients.setImageResource(R.drawable.ic_expand); // 換展開圖示
            }
        });

    }

    public void updateTotalPrice(TextView tvTotal, List<CartItem> cartItemList) {
        long totalAmount = 0;
        for (CartItem item : cartItemList) {
            totalAmount += item.getItem_price() * item.getItem_quantity();
        }
        tvTotal.setText("$" + totalAmount);
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemPhoto;
        TextView tvItemName, tvIngredients, tvQuantity, tvPrice;
        ImageButton ibTrash, ibShowIngredients;
        ImageButton ibMinus, ibPlus;


        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemPhoto = itemView.findViewById(R.id.iv_item_in_cart);
            tvItemName = itemView.findViewById(R.id.tv_item_in_cart);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients_in_cart);
            tvQuantity = itemView.findViewById(R.id.tv_num_in_cart);
            tvPrice = itemView.findViewById(R.id.tv_price_in_cart);
            ibTrash = itemView.findViewById(R.id.ib_trash_in_cart);
            ibShowIngredients = itemView.findViewById(R.id.ib_show_ingredients);
            ibMinus = itemView.findViewById(R.id.ib_minus_num_in_cart);
            ibPlus = itemView.findViewById(R.id.ib_plus_num_in_cart);


        }
    }

}
