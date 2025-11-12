package com.example.myapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Models.Product;
import com.example.myapplication.R;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private OnProductClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(android.content.Context context, List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
        Locale vietnamLocale = new Locale.Builder().setLanguage("vi").setRegion("VN").build();
        this.currencyFormat = NumberFormat.getCurrencyInstance(vietnamLocale);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvProductPrice, tvProductRating;
        private ImageView imgProduct;
        private CardView cardProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductRating = itemView.findViewById(R.id.tvProductRating);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            cardProduct = itemView.findViewById(R.id.cardProduct);

            cardProduct.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(productList.get(position));
                }
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(currencyFormat.format(product.getPrice()));
            tvProductRating.setText("⭐ " + product.getRating() + " (" + product.getReviewCount() + ")");
            
            // Set default image (có thể thay bằng Glide để load ảnh từ URL)
            imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}

