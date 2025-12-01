package thanh.toan.duan1.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class ProductDetailActivity extends Activity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private Button addToCartButton;
    private ImageButton backButton; // changed to ImageButton to match XML

    private apiProducts api;

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

                displayProduct(product);
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
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

        addToCartButton.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show()
        );
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
