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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        // 🗑️ 刪除按鈕點擊事件
        holder.ibTrash.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) return;

            String uid = currentUser.getUid();

            // 先刪除 cart_item 裡的該筆資料（找不到 ID 時你需要事先存進 item 裡）
            db.collection("cart_item")
                    .whereEqualTo("item_user_id", uid)
                    .whereEqualTo("item_id", item.getItem_id()) // 確保 item_id 是唯一識別欄位
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            db.collection("cart_item").document(doc.getId()).delete();
                        }

                        // 🧮 接著更新 cart_info
                        int price = item.getItem_price() * item.getItem_quantity();
                        db.collection("cart_info").document(uid)
                                .update(
                                        "total_price", com.google.firebase.firestore.FieldValue.increment(-price),
                                        "total_quantity", com.google.firebase.firestore.FieldValue.increment(-item.getItem_quantity())
                                );

                        // ❌ 移除該項目 & 更新 RecyclerView
                        int removeIndex = holder.getAdapterPosition();
                        itemList.remove(removeIndex);
                        notifyItemRemoved(removeIndex);

                        Toast.makeText(context, "已移除項目", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemPhoto;
        TextView tvItemName, tvIngredients, tvQuantity, tvPrice;
        ImageButton ibTrash;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemPhoto = itemView.findViewById(R.id.iv_item_in_cart);
            tvItemName = itemView.findViewById(R.id.tv_item_in_cart);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients_in_cart);
            tvQuantity = itemView.findViewById(R.id.tv_num_in_cart);
            tvPrice = itemView.findViewById(R.id.tv_price_in_cart);
            ibTrash = itemView.findViewById(R.id.ib_trash_in_cart);
        }
    }
}
