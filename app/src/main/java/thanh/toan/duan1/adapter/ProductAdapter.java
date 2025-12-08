package thanh.toan.duan1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiFavorite;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.request.WishlistResponse;
import thanh.toan.duan1.ui.ProductDetailActivity;
import thanh.toan.duan1.utils.CartManager;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnFavoriteClickListener favoriteClickListener;
    private Set<String> favoriteIds = new HashSet<>();
    // track productIds with optimistic pending changes to avoid external reloads overwriting UI
    private final Set<String> pendingChanges = new HashSet<>();
    // controls visibility of Add-to-cart button in item layout
    private boolean showAddToCart = true;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isFavorite, int position);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        // initialize favorites from local storage so UI reflects user's latest local choice
        try {
            java.util.Set<String> stored = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getStringSet("local_fav_ids", null);
            if (stored != null) favoriteIds.addAll(stored);
        } catch (Exception ignored) {}
    }

    private void persistFavorites() {
        try {
            java.util.Set<String> copy = new java.util.HashSet<>(favoriteIds);
            context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit().putStringSet("local_fav_ids", copy).apply();
        } catch (Exception ignored) {}
    }

    /**
     * When false, the adapter will hide the Add-to-cart button (useful for wishlist screen where
     * items can only be removed).
     */
    public void setShowAddToCart(boolean show) {
        this.showAddToCart = show;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.name.setText(product.getName());
        holder.price.setText(formatPrice(product.getPrice()));

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            holder.category.setText(product.getCategory().getName());
        } else {
            holder.category.setText(context.getString(R.string.no_category));
        }

        // Load ảnh. Use helper to build absolute URL for relative paths.
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String img = product.getImages().get(0);
            String url = buildFullImageUrl(img);
            Log.d("ProductAdapter", "image raw='" + img + "' -> url='" + url + "'");
            if (holder.image != null) {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.image);
            }
        } else {
            if (holder.image != null) holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // favorite icon state
        boolean isFav = favoriteIds.contains(product.getId());
        if (holder.favorite != null) {
            int resId = isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border;
            holder.favorite.setImageResource(resId);
            if (isFav) holder.favorite.setColorFilter(0xFFFF0000);
            else if (holder.favorite != null) holder.favorite.clearColorFilter();

            // Click heart để toggle favorite (optimistic UI). Do not broadcast immediately — broadcast only on server success
            holder.favorite.setOnClickListener(v -> {
                boolean currentlyFav = favoriteIds.contains(product.getId());
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                // capture position and id for callbacks
                final String pid = product.getId();

                // optimistically toggle and mark as pending so external updates won't override immediately
                if (currentlyFav) favoriteIds.remove(pid); else favoriteIds.add(pid);
                // persist local choice immediately so UI stays consistent across reloads
                persistFavorites();
                pendingChanges.add(pid);
                notifyItemChanged(pos);

                if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(product, currentlyFav, pos);

                // create apiFav instance and read token; require user logged in
                apiFavorite apiFav = ApiService.getApi(context).create(apiFavorite.class);
                String token = null;
                try { token = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("token", null); } catch (Exception ignored) {}
                if (token == null || token.isEmpty()) {
                    // Not logged in: inform user but keep optimistic UI change locally (no server sync)
                    try { Toast.makeText(context, "Vui lòng đăng nhập để đồng bộ yêu thích", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                    // Clear pending mark immediately so future server-driven refreshes can take effect when user logs in
                    pendingChanges.remove(pid);
                    return;
                }
                String bearer = "Bearer " + token;

                if (currentlyFav) {
                    // user requested remove
                    apiFav.removeFromWishlist(bearer, pid).enqueue(new Callback<WishlistResponse>() {
                        @Override
                        public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                            if (response.isSuccessful()) {
                                Log.d("ProductAdapter", "removeFromWishlist success code=" + response.code());
                                try { Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                                // notify other parts (wishlist) to refresh
                                try {
                                    Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                    intent.putExtra("productId", pid);
                                    intent.putExtra("action", "remove");
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                } catch (Exception ignored) {}
                            } else {
                                // server error — inform user but keep optimistic UI state
                                try { Toast.makeText(context, "Không thể xóa trên server", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                            }
                            // remove pending mark in all cases so future syncs can proceed
                            pendingChanges.remove(pid);
                            // persist merged favorites after server confirms
                            persistFavorites();
                        }

                        @Override
                        public void onFailure(Call<WishlistResponse> call, Throwable t) {
                            // network failure — inform user but keep optimistic UI state
                            pendingChanges.remove(pid);
                            try { Toast.makeText(context, "Lỗi kết nối: không thể xóa", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                            // revert local state on failure to reflect server unchanged state: we attempt to add back locally and notify wishlist to refresh
                            try {
                                favoriteIds.add(pid);
                                persistFavorites();
                                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                intent.putExtra("productId", pid);
                                intent.putExtra("action", "add");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            } catch (Exception ignored) {}
                        }
                    });
                } else {
                    // user requested add
                    apiFav.addToWishlist(bearer, pid).enqueue(new Callback<WishlistResponse>() {
                        @Override
                        public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                            if (response.isSuccessful()) {
                                Log.d("ProductAdapter", "addToWishlist success code=" + response.code());
                                try { Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                                try {
                                    Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                    intent.putExtra("productId", pid);
                                    intent.putExtra("action", "add");
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                } catch (Exception ignored) {}
                            } else {
                                // server error — inform user but keep optimistic UI state
                                try { Toast.makeText(context, "Không thể thêm trên server", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                            }
                            // remove pending mark so future syncs can proceed
                            pendingChanges.remove(pid);
                            // persist merged favorites after server confirms
                            persistFavorites();
                        }

                        @Override
                        public void onFailure(Call<WishlistResponse> call, Throwable t) {
                            // network failure — inform user but keep optimistic UI state
                            pendingChanges.remove(pid);
                            try { Toast.makeText(context, "Lỗi kết nối: không thể thêm", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                            // revert local state on failure
                            try {
                                favoriteIds.remove(pid);
                                persistFavorites();
                                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                                intent.putExtra("productId", pid);
                                intent.putExtra("action", "remove");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            } catch (Exception ignored) {}
                        }
                    });
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });


        holder.image.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });

        // Add to cart button handling
        if (holder.btnAddToCart != null) {
            holder.btnAddToCart.setVisibility(showAddToCart ? View.VISIBLE : View.GONE);
            if (showAddToCart) {
                holder.btnAddToCart.setOnClickListener(v -> {
                    try {
                        // Use CartHelper to try server sync when possible, fallback to local
                        thanh.toan.duan1.utils.CartHelper.addToCart(context, product, 1);
                        // Immediate user feedback when adding from product list
                        try { Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show(); } catch (Exception ignored) {}
                    } catch (Exception e) {
                        Toast.makeText(context, "Lỗi thêm vào giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.btnAddToCart.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public List<Product> getProductList() {
        return productList;
    }

    // Replace product list without recreating adapter so UI state (favorites) is preserved
    public void setProducts(List<Product> products) {
        if (products == null) {
            this.productList = new java.util.ArrayList<>();
        } else {
            this.productList = products;
        }
        notifyDataSetChanged();
    }

    public void setFavorites(Set<String> favIds) {
        if (favIds == null) return;
        // Keep any local favorites (user actions) and add server favorites if missing.
        // Do NOT remove any existing local favorites here — local UI choice takes precedence.
        for (String id : favIds) {
            if (!favoriteIds.contains(id)) favoriteIds.add(id);
        }
        // notify bound views to refresh icons
        persistFavorites();
        notifyDataSetChanged();
    }

    public void updateFavorite(String productId, boolean isFavorite) {
        if (isFavorite) favoriteIds.add(productId); else favoriteIds.remove(productId);
        // persist change so other screens can read local state
        persistFavorites();
        int pos = findPositionById(productId);
        if (pos != -1) notifyItemChanged(pos);
    }

    private int findPositionById(String productId) {
        if (productId == null || productList == null) return -1;
        for (int i = 0; i < productList.size(); i++) {
            Product p = productList.get(i);
            if (p != null && productId.equals(p.getId())) return i;
        }
        return -1;
    }

    private String formatPrice(Double price) {
        if (price == null) return "₫0";
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        return "₫" + nf.format(price.longValue());
    }

    // helper to build full image URL; mirrors ProductDetailActivity logic so both places behave consistently
    private String buildFullImageUrl(String img) {
        if (img == null) return "";
        if (img.startsWith("http")) return img;
        String base = ApiService.BASE_URL != null ? ApiService.BASE_URL : "http://10.0.2.2:3000/";
        // remove trailing slash(es)
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        // remove trailing /api if present
        if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
        if (base.endsWith("api")) base = base.substring(0, base.length() - 3);
        // ensure img starts with slash
        if (!img.startsWith("/")) img = "/" + img;
        return base + img;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton favorite; // use ImageButton to match XML (AppCompatImageButton is fine too)
        TextView name, price, category;
        View btnAddToCart; // use generic View to avoid ClassCastException when XML uses MaterialButton or ImageButton

        @SuppressLint("WrongViewCast")
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            favorite = itemView.findViewById(R.id.wishlist_button);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
         }
     }
 }
