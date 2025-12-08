package thanh.toan.duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import thanh.toan.duan1.R;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Object> banners; // can be Integer (resId) or String (url)

    public BannerAdapter(List<Object> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (banners == null || banners.isEmpty()) return;
        Object item = banners.get(position % banners.size());
        if (item instanceof Integer) {
            holder.image.setImageResource((Integer) item);
        } else if (item instanceof String) {
            String url = (String) item;
            try {
                Glide.with(holder.image.getContext()).load(url).centerCrop().into(holder.image);
            } catch (Exception e) {
                // fallback to empty
                holder.image.setImageResource(R.drawable.ic_banner_placeholder);
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_banner_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return banners == null ? 0 : banners.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.banner_image);
        }
    }
}
