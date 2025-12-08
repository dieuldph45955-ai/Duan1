package thanh.toan.duan1.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.utils.CartHelper;

public class ProductDetailActivity extends Activity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private Button addToCartButton;
    private ImageButton backButton; // changed to ImageButton to match XML
    private Spinner sizeSpinner; // new
    private android.view.View sizeContainer;

    private apiProducts api;
    private Product currentProduct; // store fetched product so we can add to cart

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        api = ApiService.getApi(this).create(apiProducts.class);

        // Lấy productId từ Intent
        String currentProductId = getIntent().getStringExtra("productId");

        if (currentProductId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchProductDetail(currentProductId);
        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        productImage = findViewById(R.id.product_detail_image);
        productName = findViewById(R.id.product_detail_name);
        productPrice = findViewById(R.id.product_detail_price);
        productDescription = findViewById(R.id.product_detail_description);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        backButton = findViewById(R.id.back_button); // now ImageButton
        sizeSpinner = findViewById(R.id.product_size_spinner); // may be null if not present in layout
        sizeContainer = findViewById(R.id.product_size_container);
    }

    private void fetchProductDetail(String productId) {
        api.getProductDetail(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }

                Product product = response.body();
                if (product == null) return;

                currentProduct = product;
                displayProduct(product);
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                // Failure intentionally silent: do not show a toast here to avoid the notification bubble
            }
        });
    }

    private void displayProduct(Product product) {
        // Load ảnh đầu tiên
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String img = product.getImages().get(0);
            String url = buildFullImageUrl(img);
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(productImage);
        }

        productName.setText(product.getName());

        NumberFormat vn = NumberFormat.getInstance(new Locale("vi", "VN"));
        productPrice.setText(vn.format(product.getPrice()) + " VNĐ");

        productDescription.setText(product.getDescription());

        // Ensure size container is visible (so user sees size area)
        if (sizeContainer != null) sizeContainer.setVisibility(android.view.View.VISIBLE);

        // Populate sizes into spinner. If the product provides sizes use them, otherwise use default set.
        if (sizeSpinner != null) {
            java.util.List<String> sizeStrings = new java.util.ArrayList<>();

            try {
                java.util.List<Object> sizes = product.getSizes();
                if (sizes != null && !sizes.isEmpty()) {
                    for (Object s : sizes) {
                        if (s == null) continue;
                        sizeStrings.add(s.toString());
                    }
                }
            } catch (Exception ignored) {
                // ignore parsing errors from product.getSizes()
            }

            // If still empty, use the requested default sizes in the order provided by the user
            if (sizeStrings.isEmpty()) {
                sizeStrings.add("S");
                sizeStrings.add("M");
                sizeStrings.add("L");
                sizeStrings.add("XXL");
                sizeStrings.add("XL");
            }

            android.widget.ArrayAdapter<String> sizeAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sizeStrings);
            sizeSpinner.setAdapter(sizeAdapter);
            if (sizeAdapter.getCount() > 0) sizeSpinner.setSelection(0);

            sizeSpinner.setEnabled(true);
            if (sizeContainer != null) sizeContainer.setVisibility(android.view.View.VISIBLE);
            else sizeSpinner.setVisibility(android.view.View.VISIBLE);
        }

        addToCartButton.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Sản phẩm chưa sẵn sàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedSize = null;
            if (sizeSpinner != null && sizeSpinner.getAdapter() != null && sizeSpinner.getSelectedItem() != null) {
                selectedSize = sizeSpinner.getSelectedItem().toString();
            }

            // Attach selected size into product before adding to cart if your Product model supports it.
            // If Product model does not have a size field, CartManager stores the whole Product object — you may
            // want to update Item or Cart logic to capture selected size separately. For now we proceed to add product.

            CartHelper.addToCart(this, currentProduct, 1);
            String message = "Đã thêm vào giỏ" + (selectedSize != null ? (" - Size: " + selectedSize) : "");
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private String buildFullImageUrl(String img) {
        if (img == null) return "";
        if (img.startsWith("http")) return img;
        String base = ApiService.BASE_URL;
        // normalize base
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
        if (base.endsWith("api")) base = base.substring(0, base.length() - 3);
        if (!img.startsWith("/")) img = "/" + img;
        return base + img;
    }

}
