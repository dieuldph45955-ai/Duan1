package thanh.toan.duan1.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.ProductAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.model.Product;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView productsRecyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private SearchView searchView;
    private String currentCategory = null;
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        productsRecyclerView = view.findViewById(R.id.products_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        searchView = view.findViewById(R.id.search_view);

        if (productsRecyclerView != null) {
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            Log.d(TAG, "RecyclerView initialized");
        } else {
            Log.e(TAG, "RecyclerView is null!");
        }

        setupSearchView();
        loadProducts(null, "");

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearch = query;
                loadProducts(currentCategory, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearch = newText;
                // Optional: Add delay to avoid too many requests while typing
                loadProducts(currentCategory, newText);
                return true;
            }
        });
    }

    private void loadProducts(String category, String search) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading products - Category: " + category + ", Search: " + search);

        apiProducts api = ApiService.getApi(getContext()).create(apiProducts.class);
        Call<List<Product>> call = api.getAllProducts(category, search);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Response received. Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "Products count: " + products.size());

                    if (products.size() > 0) {
                        Product firstProduct = products.get(0);
                        Log.d(TAG, "First product: " + firstProduct.getName());
                        Log.d(TAG, "Images: " + (firstProduct.getImages() != null ? firstProduct.getImages().size() : "null"));
                        Log.d(TAG, "Rating: " + firstProduct.getRating());
                        Log.d(TAG, "Reviews: " + (firstProduct.getReviews() != null ? firstProduct.getReviews().size() : "null"));
                    }

                    adapter = new ProductAdapter(getContext(), products);
                    productsRecyclerView.setAdapter(adapter);
                    Log.d(TAG, "Adapter set successfully");
                } else {
                    Log.e(TAG, "Response not successful or body is null");
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API Error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}