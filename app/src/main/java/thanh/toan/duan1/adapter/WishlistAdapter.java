package thanh.toan.duan1.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    public interface WishlistActionListener {
        void onRemove(Product product, int position);
    }

    private final Context context;
    private final List<Product> products;
    private final WishlistActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    public WishlistAdapter(Context context, List<Product> products, WishlistActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist_product, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = products.get(position);

        holder.name.setText(product.getName());
        holder.price.setText(formatCurrency(product.getPrice()));

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            holder.category.setText(product.getCategory().getName());
        } else {
            holder.category.setText("Không có danh mục");
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(context)
                    .load(product.getImages().get(0))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnViewDetail.setOnClickListener(v -> openProductDetail(product));
        holder.itemView.setOnClickListener(v -> openProductDetail(product));

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(product, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        context.startActivity(intent);
    }

    private String formatCurrency(Double price) {
        if (price == null) {
            return "0 đ";
        }
        return currencyFormat.format(price) + " đ";
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView price;
        TextView category;
        Button btnViewDetail;
        ImageButton btnRemove;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            category = itemView.findViewById(R.id.product_category);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
