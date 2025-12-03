package thanh.toan.duan1.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiFavorite;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.request.WishlistResponse;
import thanh.toan.duan1.ui.ProductDetailActivity;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.VH> {
    private final Context context;
    // keep the reference to the list passed from fragment so both stay in sync
    private final List<Product> items;

    // debounce timestamps to avoid double-tap actions
    private static final long CLICK_DEBOUNCE_MS = 500L;
    private long lastViewDetailClick = 0L;

    public interface OnRemoveListener {
        void onRemoved(int position);
    }

    private OnRemoveListener removeListener;

    public void setOnRemoveListener(OnRemoveListener l) { this.removeListener = l; }

    public WishlistAdapter(Context ctx, List<Product> initial) {
        this.context = ctx;
        // use the passed list reference if available so fragment and adapter share the same backing list
        if (initial != null) this.items = initial;
        else this.items = new ArrayList<>();
    }

    public void replaceAll(List<Product> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Remove an item by product id. Returns true if removed.
     */
    public boolean removeById(String productId) {
        if (productId == null) return false;
        for (int i = 0; i < items.size(); i++) {
            Product p = items.get(i);
            if (p != null && productId.equals(p.getId())) {
                items.remove(i);
                notifyItemRemoved(i);
                if (removeListener != null) removeListener.onRemoved(i);
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wishlist_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.name.setText(p.getName() != null ? p.getName() : "");
        holder.price.setText(formatPrice(p.getPrice()));
        holder.category.setText(p.getCategory() != null && p.getCategory().getName() != null ? p.getCategory().getName() : "");

        if (p.getImages() != null && !p.getImages().isEmpty()) {
            String img = p.getImages().get(0);
            String url = buildFullImageUrl(img);
            Glide.with(context).load(url).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // Ensure remove button can receive touch events
        if (holder.remove != null) {
            holder.remove.setClickable(true);
            holder.remove.setEnabled(true);
        }


        holder.remove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();

            if (pos == RecyclerView.NO_POSITION || pos < 0 || pos >= items.size()) {
                return;
            }

            final Product removedProduct = items.get(pos);
            final String removedId = removedProduct != null ? removedProduct.getId() : null;
            if (removedId == null) {
                return;
            }

            // clear pressed/selected state to avoid visual glitch on immediate removal
            try {
                v.setPressed(false);
                v.setSelected(false);
            } catch (Exception ignored) {}

            // Optimistic local removal
            try {
                items.remove(pos);
                notifyItemRemoved(pos);
                // also notify range changed so RecyclerView updates binding positions immediately
                notifyItemRangeChanged(pos, items.size() - pos);
                if (removeListener != null) removeListener.onRemoved(pos);

                // update local saved favorites immediately so other screens (Home) read consistent state
                try {
                    java.util.Set<String> stored = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getStringSet("local_fav_ids", null);
                    java.util.Set<String> copy = stored != null ? new java.util.HashSet<>(stored) : new java.util.HashSet<>();
                    copy.remove(removedId);
                    context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit().putStringSet("local_fav_ids", copy).apply();
                } catch (Exception ignored) {}

            } catch (Exception e) {
                // nothing to revert visually here - just inform user
                return;
            }

            // Fire API call; broadcast updates so other screens (Home) can update icons
            try {
                apiFavorite apiFav = ApiService.getApi(context).create(apiFavorite.class);
                String token = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("token", null);
                String bearer = token != null ? ("Bearer " + token) : null;

                apiFav.removeFromWishlist(bearer, removedId).enqueue(new Callback<WishlistResponse>() {
                    @Override
                    public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                            // notify other parts of app about removal
                            try {
                                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                intent.putExtra("productId", removedId);
                                intent.putExtra("action", "remove");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            } catch (Exception ignored) {}
                        } else {
                            // server returned error - inform user and broadcast revert so Home can restore icon
                            try { Toast.makeText(context, "Không thể xóa trên server", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                            try {
                                // revert local saved favorites
                                java.util.Set<String> stored = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getStringSet("local_fav_ids", null);
                                java.util.Set<String> copy = stored != null ? new java.util.HashSet<>(stored) : new java.util.HashSet<>();
                                copy.add(removedId);
                                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit().putStringSet("local_fav_ids", copy).apply();
                            } catch (Exception ignored) {}
                            try {
                                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                intent.putExtra("productId", removedId);
                                intent.putExtra("action", "add");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            } catch (Exception ignored) {}
                        }
                    }

                    @Override
                    public void onFailure(Call<WishlistResponse> call, Throwable t) {
                        // network failure - inform user and broadcast revert so Home can restore icon
                        try { Toast.makeText(context, "Lỗi kết nối: không thể xóa trên server", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                        try {
                            // revert local saved favorites
                            java.util.Set<String> stored = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getStringSet("local_fav_ids", null);
                            java.util.Set<String> copy = stored != null ? new java.util.HashSet<>(stored) : new java.util.HashSet<>();
                            copy.add(removedId);
                            context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit().putStringSet("local_fav_ids", copy).apply();
                        } catch (Exception ignored) {}
                        try {
                            Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                            intent.putExtra("productId", removedId);
                            intent.putExtra("action", "add");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        } catch (Exception ignored) {}
                    }
                });
            } catch (Exception e) {
                // failed to start network request - inform user but keep the item removed locally
                try { Toast.makeText(context, "Lỗi: không thể xử lý yêu cầu", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                // broadcast revert so other screens restore favorite icon
                try {
                    Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                    intent.putExtra("productId", removedId);
                    intent.putExtra("action", "add");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                } catch (Exception ignored) {}
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String formatPrice(Double price) {
        if (price == null) return "₫0";
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        return "₫" + nf.format(price.longValue());
    }

    private String buildFullImageUrl(String img) {
        if (img == null) return "";
        if (img.startsWith("http")) return img;
        String base = ApiService.BASE_URL != null ? ApiService.BASE_URL : "http://10.0.2.2:3000/";
        // normalize base
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
        if (base.endsWith("api")) base = base.substring(0, base.length() - 3);
        if (!img.startsWith("/")) img = "/" + img;
        return base + img;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, category;

        ImageView remove;

        public VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);

            remove = itemView.findViewById(R.id.btn_remove);
        }
    }

}
