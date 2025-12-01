package thanh.toan.duan1.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.WishlistAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.User;

public class WishlistFragment extends Fragment {
    private RecyclerView wishlistRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private WishlistAdapter adapter;
    private List<Product> wishlistProducts;
    private Button btnGoShopping;

    private BroadcastReceiver wishlistReceiver;

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
        btnGoShopping = view.findViewById(R.id.btn_go_shopping);
        if (btnGoShopping != null) {
            btnGoShopping.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(requireContext(), Class.forName("thanh.toan.duan1.ui.MainActivity"));
                    startActivity(i);
                } catch (ClassNotFoundException e) {
                    if (getActivity() != null) getActivity().finish();
                }
            });
        }
    }

    private void setupRecyclerView() {
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wishlistProducts = new ArrayList<>();
        adapter = new WishlistAdapter(getContext(), wishlistProducts);
        adapter.setOnRemoveListener(position -> {
            if (adapter.getItemCount() == 0) showEmptyState();
        });
        wishlistRecyclerView.setAdapter(adapter);
    }

    private void loadWishlist() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
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
        api.getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    List<Product> products = new ArrayList<>();
                    List<?> rawWishlist = user.getWishlist();

                    if (rawWishlist == null || rawWishlist.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    Gson gson = new Gson();
                    List<String> idList = new ArrayList<>();

                    for (Object item : rawWishlist) {
                        if (item instanceof Product) {
                            products.add((Product) item);
                        } else if (item instanceof String) {
                            idList.add((String) item);
                        } else {
                            try {
                                String json = gson.toJson(item);
                                Product p = gson.fromJson(json, Product.class);
                                if (p != null && p.getId() != null) {
                                    products.add(p);
                                } else if (p != null && (p.getImages() == null || p.getImages().isEmpty()) && json != null) {
                                    String trimmed = json.replace("\"", "").trim();
                                    if (!trimmed.isEmpty()) idList.add(trimmed);
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    if (!idList.isEmpty()) {
                        apiProducts prodApi = ApiService.getApi(getContext()).create(apiProducts.class);
                        AtomicInteger remaining = new AtomicInteger(idList.size());

                        for (String pid : idList) {
                            if (pid == null) {
                                if (remaining.decrementAndGet() == 0) {
                                    finalizeWishlist(products);
                                }
                                continue;
                            }

                            prodApi.getProductDetail(pid).enqueue(new Callback<Product>() {
                                @Override
                                public void onResponse(Call<Product> call, Response<Product> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        products.add(response.body());
                                    }
                                    if (remaining.decrementAndGet() == 0) {
                                        finalizeWishlist(products);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Product> call, Throwable t) {
                                    if (remaining.decrementAndGet() == 0) {
                                        finalizeWishlist(products);
                                    }
                                }
                            });
                        }
                    } else {
                        finalizeWishlist(products);
                    }

                } else {
                    Toast.makeText(getContext(), "Không tải được danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void finalizeWishlist(List<Product> products) {
        if (products == null || products.isEmpty()) {
            showEmptyState();
            return;
        }
        adapter.replaceAll(products);
        wishlistProducts.clear();
        wishlistProducts.addAll(products);
        if (products.isEmpty()) showEmptyState();
        else showWishlist();
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
        loadWishlist();
    }

    @Override
    public void onStart() {
        super.onStart();
        wishlistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                String productId = intent.getStringExtra("productId");
                if (action == null) {
                    loadWishlist();
                    return;
                }
                switch (action) {
                    case "remove":
                        if (productId != null) {
                            try {
                                adapter.removeById(productId);
                                Iterator<Product> it = wishlistProducts.iterator();
                                while (it.hasNext()) {
                                    Product pr = it.next();
                                    if (pr != null && productId.equals(pr.getId())) {
                                        it.remove();
                                    }
                                }
                            } catch (Exception ignored) {}
                            if (adapter.getItemCount() == 0) showEmptyState();
                        }
                        break;
                    case "add":
                        loadWishlist();
                        break;
                    default:
                        loadWishlist();
                }
             }
         };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(wishlistReceiver, new IntentFilter("thanh.toan.duan1.WISHLIST_UPDATED"));
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(wishlistReceiver);
        } catch (Exception ignored) {}
     }
}
