package thanh.toan.duan1.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiFavorite;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.request.WishlistResponse;
import thanh.toan.duan1.ui.ProductDetailActivity;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;

    private Set<String> favoriteIds = new HashSet<>();

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public ProductAdapter(Context context, List<Product> productList, Set<String> favoriteIds) {
        this.context = context;
        this.productList = productList;
        if (favoriteIds != null) this.favoriteIds = favoriteIds;
    }

    public void setFavorites(Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds != null ? favoriteIds : new HashSet<>();
        notifyDataSetChanged();
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

        // Hiển thị thông tin sản phẩm
        holder.name.setText(product.getName());
        holder.price.setText(formatPrice(product.getPrice()));
        holder.category.setText(product.getCategory() != null ? product.getCategory().getName() : "No category");
        holder.ratingBar.setRating(product.getRating() != null ? product.getRating().floatValue() : 0);
        holder.reviewCount.setText(product.getReviews() != null ? product.getReviews().size() + " reviews" : "0 reviews");

        // Load ảnh
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // Lấy token
        SharedPreferences pref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String tokenRaw = pref.getString("token", "");
        boolean isLoggedIn = !tokenRaw.isEmpty();

        // API favorite
        apiFavorite apiFav = ApiService.getApi(context).create(apiFavorite.class);

        // Set heart based on favoritesSet (fetched once by fragment)
        boolean isFav = favoriteIds != null && favoriteIds.contains(product.getId());
        holder.wishlistButton.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        // Click heart để toggle favorite
        holder.wishlistButton.setOnClickListener(v -> {
            if (!isLoggedIn) {
                Toast.makeText(context, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
                return;
            }

            // Optimistic toggle: update UI immediately and local set, then call API
            boolean currentlyFav = favoriteIds != null && favoriteIds.contains(product.getId());
            if (currentlyFav) {
                // optimistic remove
                holder.wishlistButton.setImageResource(R.drawable.ic_favorite_border);
                if (favoriteIds != null) favoriteIds.remove(product.getId());
                // notify wishlist changed (application broadcast)
                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                context.getApplicationContext().sendBroadcast(intent);

                apiFav.removeFromWishlist(product.getId()).enqueue(new Callback<WishlistResponse>() {
                    @Override
                    public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                        Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<WishlistResponse> call, Throwable t) {
                        // revert on failure
                        if (favoriteIds != null) favoriteIds.add(product.getId());
                        holder.wishlistButton.setImageResource(R.drawable.ic_favorite_filled);
                        // notify wishlist changed (application broadcast)
                        Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                        context.getApplicationContext().sendBroadcast(intent);
                        Toast.makeText(context, "Xoá thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // optimistic add
                holder.wishlistButton.setImageResource(R.drawable.ic_favorite_filled);
                if (favoriteIds != null) favoriteIds.add(product.getId());
                // notify wishlist changed (application broadcast)
                Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                context.getApplicationContext().sendBroadcast(intent);

                apiFav.addToWishlist(product.getId()).enqueue(new Callback<WishlistResponse>() {
                    @Override
                    public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                        Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<WishlistResponse> call, Throwable t) {
                        // revert on failure
                        if (favoriteIds != null) favoriteIds.remove(product.getId());
                        holder.wishlistButton.setImageResource(R.drawable.ic_favorite_border);
                        // notify wishlist changed (application broadcast)
                        Intent intent = new Intent("thanh.toan.duan1.WISHLIST_UPDATED");
                        context.getApplicationContext().sendBroadcast(intent);
                        Toast.makeText(context, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Click item mở chi tiết
        View.OnClickListener openDetail = v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        };
        holder.itemView.setOnClickListener(openDetail);
        holder.image.setOnClickListener(openDetail);

        // Button Add to Cart
        holder.btnAddToCart.setOnClickListener(v ->
                Toast.makeText(context, product.getName() + " added to cart", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private String formatPrice(Double price) {
        if (price == null) return "₫0";
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        return "₫" + nf.format(price.longValue());
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image, wishlistButton;
        TextView name, price, category, reviewCount;
        RatingBar ratingBar;
        MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            wishlistButton = itemView.findViewById(R.id.wishlist_button);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);
            ratingBar = itemView.findViewById(R.id.product_rating);
            reviewCount = itemView.findViewById(R.id.product_review_count);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
