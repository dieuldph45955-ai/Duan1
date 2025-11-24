package thanh.toan.duan1.fragment;

import android.content.Context;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.WishlistAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.User;
import thanh.toan.duan1.ui.MainActivity;

public class WishlistFragment extends Fragment implements WishlistAdapter.WishlistActionListener {
    private static final String TAG = "WishlistFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private Button btnGoShopping;

    private WishlistAdapter adapter;
    private final List<Product> wishlistProducts = new ArrayList<>();
    private apiProfile api;
    private String token;
    private Context appContext;
    private Call<User> currentCall;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        appContext = null;
        cancelCurrentCall();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupButtons();

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem yêu thích", Toast.LENGTH_SHORT).show();
            showEmptyState();
        } else {
            api = ApiService.getApi(requireContext()).create(apiProfile.class);
            fetchWishlist(false);
        }

        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.wishlist_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyLayout = view.findViewById(R.id.empty_layout);
        btnGoShopping = view.findViewById(R.id.btn_go_shopping);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WishlistAdapter(requireContext(), wishlistProducts, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchWishlist(true));
        swipeRefreshLayout.setColorSchemeResources(R.color.purple_500, R.color.teal_700);
    }

    private void setupButtons() {
        btnGoShopping.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                mainActivity.getBottomNavigationView().setSelectedItemId(R.id.navigation_home);
            }
        });
    }

    private void fetchWishlist(boolean isRefreshing) {
        if (token == null || api == null) {
            return;
        }

        if (!isRefreshing) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        }

        cancelCurrentCall();
        currentCall = api.getUserProfile("Bearer " + token);
        currentCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getWishlist();
                    wishlistProducts.clear();
                    if (products != null) {
                        wishlistProducts.addAll(products);
                    }

                    if (wishlistProducts.isEmpty()) {
                        showEmptyState();
                    } else {
                        adapter.notifyDataSetChanged();
                        showWishlist();
                    }
                } else {
                    showToastSafe("Không tải được danh sách yêu thích");
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                Log.e(TAG, "API Error: " + t.getMessage());
                showToastSafe("Lỗi kết nối: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        currentCall = null;
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void showWishlist() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (token != null) {
            fetchWishlist(false);
        }
    }

    @Override
    public void onRemove(Product product, int position) {
        if (api == null || token == null) {
            showToastSafe("Bạn cần đăng nhập lại");
            return;
        }

        api.removeFromWishlist(product.getId(), "Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    int adapterPosition = position >= 0 ? position : wishlistProducts.indexOf(product);
                    if (adapterPosition >= 0 && adapterPosition < wishlistProducts.size()) {
                        wishlistProducts.remove(adapterPosition);
                        adapter.notifyItemRemoved(adapterPosition);
                    }

                    if (wishlistProducts.isEmpty()) {
                        showEmptyState();
                    }
                    showToastSafe("Đã xóa khỏi yêu thích");
                } else {
                    showToastSafe("Không thể xóa khỏi yêu thích");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                showToastSafe("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private long lastToastTime = 0L;

    private void showToastSafe(String message) {
        if (!isAdded() || appContext == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastToastTime < 1500) {
            return;
        }
        lastToastTime = now;
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
    }
}

