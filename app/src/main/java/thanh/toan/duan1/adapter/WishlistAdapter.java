package thanh.toan.duan1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

import thanh.toan.duan1.R;
import thanh.toan.duan1.model.Product;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<Product> wishlistProducts;
    private OnRemoveClickListener onRemoveClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(String productId);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(String productId);
    }

    public WishlistAdapter(
            List<Product> wishlistProducts,
            OnRemoveClickListener onRemoveClickListener,
            OnAddToCartClickListener onAddToCartClickListener
    ) {
        this.wishlistProducts = wishlistProducts;
        this.onRemoveClickListener = onRemoveClickListener;
        this.onAddToCartClickListener = onAddToCartClickListener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = wishlistProducts.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return wishlistProducts.size();
    }

    public void updateList(List<Product> newList) {
        this.wishlistProducts = newList;
        notifyDataSetChanged();
    }

    public class WishlistViewHolder extends RecyclerView.ViewHolder {

        private ImageView productImage;
        private TextView productName;
        private TextView productPrice;
        private ImageButton removeButton;
        private ImageButton addToCartButton;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.wishlist_product_image);
            productName = itemView.findViewById(R.id.wishlist_product_name);
            productPrice = itemView.findViewById(R.id.wishlist_product_price);
            removeButton = itemView.findViewById(R.id.remove_from_wishlist_button);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }

        public void bind(Product product) {
            productName.setText(product.getName());

            DecimalFormat decimalFormat = new DecimalFormat("#,##0");
            productPrice.setText(
                    decimalFormat.format(product.getPrice()) + " VND"
            );

            // Load image using Glide
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(productImage);
            }

            removeButton.setOnClickListener(v -> {
                onRemoveClickListener.onRemoveClick(product.getId());
            });

            addToCartButton.setOnClickListener(v -> {
                onAddToCartClickListener.onAddToCartClick(product.getId());
            });
        }
    }
}