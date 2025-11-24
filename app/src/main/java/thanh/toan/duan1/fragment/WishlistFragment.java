package thanh.toan.duan1.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.ProductAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.User;

public class WishlistFragment extends Fragment {
    private static final String TAG = "WishlistFragment";
    private RecyclerView wishlistRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private ProductAdapter adapter;
    private List<Product> wishlistProducts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        initViews(view);
        setupRecyclerView();
        loadWishlist();

        return view;
    }

    private void initViews(View view) {
        wishlistRecyclerView = view.findViewById(R.id.wishlist_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyLayout = view.findViewById(R.id.empty_layout);
    }

    private void setupRecyclerView() {
        // Dùng GridLayoutManager với 2 cột
        wishlistRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        wishlistProducts = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), wishlistProducts);
        wishlistRecyclerView.setAdapter(adapter);
    }

    private void loadWishlist() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem yêu thích", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        wishlistRecyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);

        apiProfile api = ApiService.getApi(getContext()).create(apiProfile.class);
        api.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    List<Product> products = user.getWishlist();

                    if (products != null && !products.isEmpty()) {
                        wishlistProducts.clear();
                        wishlistProducts.addAll(products);
                        adapter.notifyDataSetChanged();
                        showWishlist();
                    } else {
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(getContext(), "Không tải được danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API Error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        wishlistRecyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void showWishlist() {
        wishlistRecyclerView.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload khi quay lại fragment
        loadWishlist();
    }
}

