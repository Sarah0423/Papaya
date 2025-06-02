package ppy.app.papaya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<MapItem> favoriteList;

    public FavoriteAdapter(List<MapItem> favoriteList) {
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_add, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        MapItem item = favoriteList.get(position);
        int imgResId = holder.itemView.getResources().getIdentifier(item.getMapImg(), "mipmap", holder.itemView.getContext().getPackageName());
        holder.ivBranchInfoImg.setImageResource(imgResId);
        holder.tvBranchInfoName.setText(item.getMapName());
        holder.tvBranchInfoTel.setText(item.getMapTel());
        holder.tvBranchInfoAddress.setText(item.getMapAddress());
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBranchInfoImg;
        TextView tvBranchInfoName;
        TextView tvBranchInfoTel;
        TextView tvBranchInfoAddress;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBranchInfoImg = itemView.findViewById(R.id.iv_branch_info_img);
            tvBranchInfoName = itemView.findViewById(R.id.tv_branch_info_name);
            tvBranchInfoTel = itemView.findViewById(R.id.tv_branch_info_tel);
            tvBranchInfoAddress = itemView.findViewById(R.id.tv_branch_info_address);
        }
    }
}
