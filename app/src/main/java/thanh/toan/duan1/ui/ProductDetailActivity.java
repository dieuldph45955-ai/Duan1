package thanh.toan.duan1.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.fragment.HomeFragment;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.Review;
import thanh.toan.duan1.model.User;

public class ProductDetailActivity extends Activity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription;
    private ImageButton wishlistButton;
    private Button addToCartButton;
    private ImageButton backButton; // changed to ImageButton to match XML
    private RecyclerView reviewRecyclerView;

    private apiProducts api;
    private apiProfile apiProfile;
    private Product currentProduct;
    private boolean isInWishlist = false;
    private String currentProductId;

    private View wishlistButtonView; // generic view to avoid class cast problems

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        api = ApiService.getApi(this).create(apiProducts.class);
        apiProfile = ApiService.getApi(this).create(apiProfile.class);

        // Lấy productId từ Intent
        currentProductId = getIntent().getStringExtra("productId");

        if (currentProductId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchProductDetail(currentProductId);
        checkWishlistStatus();
        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        productImage = findViewById(R.id.product_detail_image);
        productName = findViewById(R.id.product_detail_name);
        productPrice = findViewById(R.id.product_detail_price);
        productDescription = findViewById(R.id.product_detail_description);
        wishlistButtonView = findViewById(R.id.wishlist_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        backButton = findViewById(R.id.back_button); // now ImageButton
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

        // Setup wishlist button
        if (wishlistButtonView != null) {
            wishlistButtonView.setOnClickListener(v -> {
                v.setEnabled(false);
                v.postDelayed(() -> v.setEnabled(true), 400);
                toggleWishlist();
            });
        }
    }

    private String buildFullImageUrl(String img) {
        if (img == null) return "";
        if (img.startsWith("http")) return img;
        // derive host from ApiService.BASE_URL (e.g. http://10.0.2.2:3000/api/ -> http://10.0.2.2:3000)
        String base = ApiService.BASE_URL != null ? ApiService.BASE_URL : "http://10.0.2.2:3000/";
        // remove trailing slash(es)
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        // remove trailing /api if present
        if (base.endsWith("/api")) base = base.substring(0, base.length() - 4);
        if (base.endsWith("api")) base = base.substring(0, base.length() - 3);
        // ensure img starts with slash
        if (!img.startsWith("/")) img = "/" + img;
        return base + img;
    }

    private void checkWishlistStatus() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            runOnUiThread(() -> updateWishlistIcon(false));
            return;
        }

        apiProfile.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    List<String> wishlistIds = user.getWishlist(); // <-- List<String> ID

                    if (wishlistIds != null && wishlistIds.contains(currentProductId)) {
                        isInWishlist = true;
                    } else {
                        isInWishlist = false;
                    }

                    runOnUiThread(() -> updateWishlistIcon(isInWishlist));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Không làm gì nếu lỗi
            }
        });
    }

    private void toggleWishlist() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optimistic UI: toggle immediately
        final boolean previous = isInWishlist;
        isInWishlist = !isInWishlist;
        runOnUiThread(() -> updateWishlistIcon(isInWishlist));

        if (previous) {
            // Previously favorited -> remove
            apiProfile.removeFromWishlist(currentProductId, "Bearer " + token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (!response.isSuccessful()) {
                        // revert on failure status
                        isInWishlist = previous;
                        runOnUiThread(() -> updateWishlistIcon(isInWishlist));
                        Toast.makeText(ProductDetailActivity.this, "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // revert on network failure
                    isInWishlist = previous;
                    runOnUiThread(() -> updateWishlistIcon(isInWishlist));
                    Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Previously not favorited -> add
            apiProfile.addToWishlist(currentProductId, "Bearer " + token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (!response.isSuccessful()) {
                        // revert on failure status
                        isInWishlist = previous;
                        runOnUiThread(() -> updateWishlistIcon(isInWishlist));
                        Toast.makeText(ProductDetailActivity.this, "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // revert on network failure
                    isInWishlist = previous;
                    runOnUiThread(() -> updateWishlistIcon(isInWishlist));
                    Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateWishlistIcon(boolean isFavorite) {
        if (wishlistButtonView == null) {
            // try to find it lazily
            wishlistButtonView = findViewById(R.id.wishlist_button);
            if (wishlistButtonView == null) return; // nothing we can do
        }

        // handle several possible view types safely
        if (wishlistButtonView instanceof ImageButton) {
            ImageButton ib = (ImageButton) wishlistButtonView;
            if (isFavorite) {
                ib.setImageResource(R.drawable.ic_favorite_filled);
                ImageViewCompat.setImageTintList(ib, ColorStateList.valueOf(Color.RED));
            } else {
                ib.setImageResource(R.drawable.ic_favorite_border);
                ImageViewCompat.setImageTintList(ib, null);
            }
        } else if (wishlistButtonView instanceof ImageView) {
            ImageView iv = (ImageView) wishlistButtonView;
            if (isFavorite) {
                iv.setImageResource(R.drawable.ic_favorite_filled);
                ImageViewCompat.setImageTintList(iv, ColorStateList.valueOf(Color.RED));
            } else {
                iv.setImageResource(R.drawable.ic_favorite_border);
                ImageViewCompat.setImageTintList(iv, null);
            }
        } else if (wishlistButtonView instanceof MaterialButton) {
            MaterialButton mb = (MaterialButton) wishlistButtonView;
            if (isFavorite) {
                mb.setIconResource(R.drawable.ic_favorite_filled);
                mb.setIconTint(ColorStateList.valueOf(Color.RED));
            } else {
                mb.setIconResource(R.drawable.ic_favorite_border);
                mb.setIconTint(null);
            }
        } else {
            // fallback: set background color or content description
            wishlistButtonView.setSelected(isFavorite);
        }
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
