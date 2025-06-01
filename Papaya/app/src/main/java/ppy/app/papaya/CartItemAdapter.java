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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
                        db.collection("cart_info").document(uid)
                                .update(
                                        "total_price", FieldValue.increment(-price),
                                        "total_quantity", FieldValue.increment(-item.getItem_quantity())
                                );

                        // ❌ 從列表移除
                        int removeIndex = holder.getAdapterPosition();
                        itemList.remove(removeIndex);
                        notifyItemRemoved(removeIndex);

                        Toast.makeText(context, "已移除項目", Toast.LENGTH_SHORT).show();

                        // ✅ 如果你有傳進來 tvTotal 的話，這裡也可以重新抓一次總價
                        if (tvTotal != null) {
                            db.collection("cart_info").document(uid)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            long newTotal = documentSnapshot.getLong("total_price") != null ?
                                                    documentSnapshot.getLong("total_price") : 0;
                                            tvTotal.setText("$" + newTotal);
                                        }
                                    });
                        }
                    });

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

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemPhoto;
        TextView tvItemName, tvIngredients, tvQuantity, tvPrice;
        ImageButton ibTrash, ibShowIngredients;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemPhoto = itemView.findViewById(R.id.iv_item_in_cart);
            tvItemName = itemView.findViewById(R.id.tv_item_in_cart);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients_in_cart);
            tvQuantity = itemView.findViewById(R.id.tv_num_in_cart);
            tvPrice = itemView.findViewById(R.id.tv_price_in_cart);
            ibTrash = itemView.findViewById(R.id.ib_trash_in_cart);
            ibShowIngredients = itemView.findViewById(R.id.ib_show_ingredients);

        }
    }
}
