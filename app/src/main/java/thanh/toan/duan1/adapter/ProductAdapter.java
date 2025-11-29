package thanh.toan.duan1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import thanh.toan.duan1.R;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.ui.ProductDetailActivity;
import thanh.toan.duan1.utils.CartManager;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnFavoriteClickListener favoriteClickListener;
    private Set<String> favoriteIds = new HashSet<>();

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isFavorite, int position);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
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
            holder.category.setText("No category");
        }

        if (product.getRating() != null) {
            holder.ratingBar.setRating(product.getRating().floatValue());
        } else {
            holder.ratingBar.setRating(0);
        }

        if (product.getReviews() != null) {
            holder.reviewCount.setText(product.getReviews().size() + " reviews");
        } else {
            holder.reviewCount.setText("0 reviews");
        }

        // Load ảnh. If server returns a relative path (e.g. /uploads/...), prefix emulator host.
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String img = product.getImages().get(0);
            if (img != null && !img.startsWith("http")) {
                // ensure leading slash
                if (!img.startsWith("/")) img = "/" + img;
                img = "http://10.0.2.2:3000" + img;
            }
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // favorite icon state
        boolean isFav = favoriteIds.contains(product.getId());
        if (holder.favorite != null) {
            int resId = isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border;
            try {
                holder.favorite.setImageResource(resId);
                holder.favorite.clearColorFilter();
            } catch (Exception e) {
                if (isFav) holder.favorite.setColorFilter(0xFFFF0000); else holder.favorite.clearColorFilter();
            }

            holder.favorite.setOnClickListener(v -> {
                boolean currentlyFav = favoriteIds.contains(product.getId());
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                // optimistically toggle
                if (currentlyFav) {
                    favoriteIds.remove(product.getId());
                } else {
                    favoriteIds.add(product.getId());
                }
                notifyItemChanged(pos);

                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(product, currentlyFav, pos);
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
        holder.btnAddToCart.setOnClickListener(v -> {
            try {
                CartManager cm = new CartManager(context);
                cm.addToCart(product, 1);
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Lỗi thêm vào giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setFavorites(Set<String> favIds) {
        if (favIds == null) return;
        favoriteIds.clear();
        favoriteIds.addAll(favIds);
        notifyDataSetChanged();
    }

    public void updateFavorite(String productId, boolean isFavorite) {
        if (isFavorite) favoriteIds.add(productId); else favoriteIds.remove(productId);
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        AppCompatImageButton favorite; // changed to AppCompatImageButton
        TextView name, price, category, reviewCount;
        RatingBar ratingBar;
        AppCompatImageButton btnAddToCart; // changed type to AppCompatImageButton

        @SuppressLint("WrongViewCast")
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            favorite = itemView.findViewById(R.id.wishlist_button);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);
            ratingBar = itemView.findViewById(R.id.product_rating);
            reviewCount = itemView.findViewById(R.id.product_review_count);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
