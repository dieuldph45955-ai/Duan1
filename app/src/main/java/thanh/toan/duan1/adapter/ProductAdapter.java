package thanh.toan.duan1.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import thanh.toan.duan1.R;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.ui.ProductDetailActivity;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

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
        ImageView image;
        TextView name, price, category, reviewCount;
        RatingBar ratingBar;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);
            ratingBar = itemView.findViewById(R.id.product_rating);
            reviewCount = itemView.findViewById(R.id.product_review_count);
        }
    }
}
