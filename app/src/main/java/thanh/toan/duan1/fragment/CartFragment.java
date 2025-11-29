package thanh.toan.duan1.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import thanh.toan.duan1.api.apiOrder;
import thanh.toan.duan1.model.Item;
import thanh.toan.duan1.model.Order;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.request.OrderRequest;
import thanh.toan.duan1.ui.CheckoutActivity;
import thanh.toan.duan1.utils.CartManager;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private CartManager cartManager;
    private List<Item> cartItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cart_recycler_view);
        totalPriceTextView = view.findViewById(R.id.cart_total_price);
        checkoutButton = view.findViewById(R.id.cart_checkout_button);

        cartManager = new CartManager(getContext());
        cartItems = cartManager.getCart();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(getContext(), cartItems);
        recyclerView.setAdapter(adapter);

        calculateTotal();

        checkoutButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), CheckoutActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh cart data when returning to this screen
        if (cartManager != null) {
            cartItems.clear();
            cartItems.addAll(cartManager.getCart());
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            calculateTotal();
        }
    }

    private void calculateTotal() {
        double total = 0;
        for (Item item : cartItems) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText("Total: " + nf.format(total) + " VND");
    }

    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
        private Context context;
        private List<Item> items;

        public CartAdapter(Context context, List<Item> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            Item item = items.get(position);
            Product product = item.getProduct();

            if (product != null) {
                holder.name.setText(product.getName());
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                holder.price.setText(nf.format(product.getPrice()) + " VND");

                if (product.getImages() != null && !product.getImages().isEmpty() && product.getImages().get(0) != null) {
                    String raw = product.getImages().get(0).trim();
                    // normalize backslashes and extra spaces
                    raw = raw.replace("\\", "/").trim();
                    String imgUrl;
                    if (raw.startsWith("http://") || raw.startsWith("https://")) {
                        imgUrl = raw;
                    } else {
                        // ensure single slash between host and path
                        if (raw.startsWith("/")) imgUrl = "http://10.0.2.2:3000" + raw;
                        else imgUrl = "http://10.0.2.2:3000/" + raw;
                    }
                    Glide.with(context)
                            .load(imgUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .into(holder.image);
                } else {
                    holder.image.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            holder.quantity.setText(String.valueOf(item.getQuantity()));

            holder.increaseButton.setOnClickListener(v -> {
                int newQuantity = item.getQuantity().intValue() + 1;
                item.setQuantity((long) newQuantity);
                cartManager.updateQuantity(position, newQuantity);
                notifyItemChanged(position);
                calculateTotal();
            });

            holder.decreaseButton.setOnClickListener(v -> {
                int currentQuantity = item.getQuantity().intValue();
                if (currentQuantity > 1) {
                    int newQuantity = currentQuantity - 1;
                    item.setQuantity((long) newQuantity);
                    cartManager.updateQuantity(position, newQuantity);
                    notifyItemChanged(position);
                    calculateTotal();
                } else {
                    // Nếu số lượng là 1 mà bấm giảm thì hỏi người dùng có muốn xóa không
                    cartManager.removeFromCart(position);
                    items.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, items.size());
                    calculateTotal();
                }
            });

            holder.removeButton.setOnClickListener(v -> {
                cartManager.removeFromCart(position);
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
                calculateTotal();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class CartViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView name, price, quantity;
            Button removeButton;
            ImageButton increaseButton, decreaseButton;

            public CartViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.cart_item_image);
                name = itemView.findViewById(R.id.cart_item_name);
                price = itemView.findViewById(R.id.cart_item_price);
                quantity = itemView.findViewById(R.id.cart_item_quantity);
                removeButton = itemView.findViewById(R.id.cart_item_remove);
                increaseButton = itemView.findViewById(R.id.cart_item_increase);
                decreaseButton = itemView.findViewById(R.id.cart_item_decrease);
            }
        }
    }
}
