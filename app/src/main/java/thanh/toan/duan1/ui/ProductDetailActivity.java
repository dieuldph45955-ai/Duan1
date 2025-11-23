package thanh.toan.duan1.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.ReviewAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.Review;

public class ProductDetailActivity extends Activity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private ImageButton wishlistButton;
    private Button addToCartButton;
    private RecyclerView reviewRecyclerView;

    private apiProducts api;

    public static final String BASE_URL = "http://10.0.2.2:3000/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        api = ApiService.getApi(this).create(apiProducts.class);

        // Lấy productId từ Intent
        String productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchProductDetail(productId);
    }

    private void initViews() {
        productImage = findViewById(R.id.product_detail_image);
        productName = findViewById(R.id.product_detail_name);
        productPrice = findViewById(R.id.product_detail_price);
        productDescription = findViewById(R.id.product_detail_description);
        wishlistButton = findViewById(R.id.wishlist_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);
//        reviewRecyclerView = findViewById(R.id.reviews_recycler_view);
//
//        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
//                displayReviews(product.getReviews());
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e("ProductDetail", "API Error: " + t.getMessage());
                Toast.makeText(ProductDetailActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(Product product) {
        // Load ảnh đầu tiên
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String img = product.getImages().get(0);
            if (img.startsWith("http")) {
                Glide.with(this).load(img).into(productImage);
            } else {
                Glide.with(this).load(BASE_URL + img).into(productImage);
            }
        }

        productName.setText(product.getName());

        NumberFormat vn = NumberFormat.getInstance(new Locale("vi", "VN"));
        productPrice.setText(vn.format(product.getPrice()) + " VNĐ");

        productDescription.setText(product.getDescription());

        addToCartButton.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show()
        );
    }


//    private void displayReviews(List<Review> reviews) {
//        if (reviews == null || reviews.isEmpty()) {
//            Toast.makeText(this, "No reviews available", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        List<Review> convertedReviews = new java.util.ArrayList<>();
//        for (Review pr : reviews) {
//            Review r = new Review();
//            r.setId(pr.getId());
//            r.setId(pr.getId());
//            r.setUser(pr.getUser());
//            r.setProduct(pr.getProduct());
//            r.setRating(pr.getRating());
//            r.setComment(pr.getComment());
//            r.setCreatedAt(pr.getCreatedAt());
//            convertedReviews.add(r);
//        }
//
//        ReviewAdapter reviewAdapter = new ReviewAdapter(this, convertedReviews);
//        reviewRecyclerView.setAdapter(reviewAdapter);
//    }
}
