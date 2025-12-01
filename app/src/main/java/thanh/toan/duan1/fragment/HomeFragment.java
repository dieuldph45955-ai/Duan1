package thanh.toan.duan1.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.ProductAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.ApiCategories;
import thanh.toan.duan1.api.apiProducts;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.Category;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.User;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView productsRecyclerView;
    private ProgressBar progressBar;
    private ProductAdapter adapter;
    private SearchView searchView;
    private Spinner categorySpinner;
    private String currentCategory = null;
    private String currentSearch = "";
    private String token;
    private boolean isFirstLoad = true;
    private List<Category> categories = new ArrayList<>();
    private boolean spinnerInitialized = false; // skip the initial callback fired when adapter is set

    private BroadcastReceiver wishlistReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        productsRecyclerView = view.findViewById(R.id.products_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        searchView = view.findViewById(R.id.search_view);
        categorySpinner = view.findViewById(R.id.category_spinner);

        // load token from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (productsRecyclerView != null) {
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            Log.d(TAG, "RecyclerView initialized");
        } else {
            Log.e(TAG, "RecyclerView is null!");
        }

        // no touch listener: use spinnerInitialized to ignore the first automatic onItemSelected

        setupSearchView();
        setupCategorySpinner();

        loadCategories(); // fetch categories and then load products

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register receiver for wishlist updates to update icons
        wishlistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String action = intent.getStringExtra("action");
                String productId = intent.getStringExtra("productId");
                if (action == null || productId == null) return;
                switch (action) {
                    case "remove":
                        if (adapter != null) adapter.updateFavorite(productId, false);
                        break;
                    case "add":
                        if (adapter != null) adapter.updateFavorite(productId, true);
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(wishlistReceiver, new IntentFilter("thanh.toan.duan1.WISHLIST_UPDATED"));

        // read persisted local favorites so Home icons reflect latest state even if changes occurred while Home was paused
        try {
            java.util.Set<String> stored = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getStringSet("local_fav_ids", null);
            if (stored != null && adapter != null) {
                adapter.setFavorites(new java.util.HashSet<>(stored));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(wishlistReceiver);
        } catch (Exception ignored) {}
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

    private void setupCategorySpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentCategory = null; // 'All' selected
                } else if (position - 1 >= 0 && position - 1 < categories.size()) {
                    Category selected = categories.get(position - 1);
                    // Use category id for filtering (safer if backend expects id). If backend expects name, change to getName().
                    currentCategory = selected.getId();
                } else {
                    currentCategory = null;
                }

                // Ignore the first automatic callback when adapter is set; subsequent selections are user actions
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }
                loadProducts(currentCategory, currentSearch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
    }

    private void loadCategories() {
        // create service explicitly with fully-qualified types to avoid analyzer issues
        thanh.toan.duan1.api.ApiCategories catService = ApiService.getApi(requireContext())
                .create(thanh.toan.duan1.api.ApiCategories.class);

        retrofit2.Call<java.util.List<thanh.toan.duan1.model.Category>> catCall = catService.getCategories();

        catCall.enqueue(new retrofit2.Callback<java.util.List<thanh.toan.duan1.model.Category>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<java.util.List<thanh.toan.duan1.model.Category>> call, @NonNull retrofit2.Response<java.util.List<thanh.toan.duan1.model.Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());

                    // Remove "Quần Nữ" from the spinner: normalize names to compare without diacritics
                    Iterator<Category> it = categories.iterator();
                    while (it.hasNext()) {
                        Category c = it.next();
                        if (c == null || c.getName() == null) continue;
                        if ("quan nu".equals(normalize(c.getName()))) {
                            it.remove();
                        }
                    }

                    List<String> names = new ArrayList<>();
                    names.add("Tất cả");
                    for (Category c : categories) names.add(c.getName());

                    ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // set adapter programmatically. The first onItemSelected will be ignored by spinnerInitialized.
                    categorySpinner.setAdapter(adapterSpinner);
                    categorySpinner.setSelection(0);
                } else {
                    Log.w(TAG, "Failed to load categories");
                }
                // After categories attempted to load, load products anyway
                loadProducts(currentCategory, currentSearch);
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "Category API error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi tải danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // still load products without category
                loadProducts(currentCategory, currentSearch);
            }
        });
    }

    // normalize string: remove diacritics and lower-case
    private String normalize(String s) {
        if (s == null) return "";
        String tmp = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return tmp.toLowerCase().trim();
    }

    private void loadProducts(String category, String search) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading products - Category: " + category + ", Search: " + search);

        apiProducts api = ApiService.getApi(getContext()).create(apiProducts.class);
        Call<List<Product>> call = api.getAllProducts(category, search);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Response received. Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "Products count: " + products.size());

                    if (!products.isEmpty()) {
                        Product firstProduct = products.get(0);
                        Log.d(TAG, "First product: " + firstProduct.getName());
                        Log.d(TAG, "Images: " + (firstProduct.getImages() != null ? firstProduct.getImages().size() : "null"));
                        Log.d(TAG, "Rating: " + firstProduct.getRating());
                        Log.d(TAG, "Reviews: " + (firstProduct.getReviews() != null ? firstProduct.getReviews().size() : "null"));
                    }

                    if (adapter == null) {
                        adapter = new ProductAdapter(requireContext(), products);
                        productsRecyclerView.setAdapter(adapter);
                        Log.d(TAG, "Adapter created and set");
                    } else {
                        // reuse existing adapter to preserve UI state (favorites, pending changes)
                        adapter.setProducts(products);
                        Log.d(TAG, "Adapter updated with new products");
                    }

                    // Load wishlist of logged-in user only once to avoid UI reset
                    if (token != null && isFirstLoad) {
                        isFirstLoad = false;
                        apiProfile profileApi = ApiService.getApi(requireContext()).create(apiProfile.class);
                        profileApi.getUserProfile().enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Set<String> favIds = new HashSet<>();
                                    if (response.body().getWishlist() != null) {
                                        List<?> wishlist = response.body().getWishlist();
                                        for (Object item : wishlist) {
                                            if (item == null) continue;
                                            if (item instanceof String) favIds.add((String) item);
                                            else if (item instanceof Product) {
                                                Product p = (Product) item;
                                                if (p.getId() != null) favIds.add(p.getId());
                                            } else {
                                                favIds.add(item.toString());
                                            }
                                        }
                                    }
                                    adapter.setFavorites(favIds);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                                Log.w(TAG, "Failed to load user wishlist: " + t.getMessage());
                            }
                        });
                    }

                } else {
                    Log.e(TAG, "Response not successful or body is null");
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API Error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFavoriteClick(Product product, boolean wasFavorite) {
        // Ensure token available
        if (token == null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            token = prefs.getString("token", null);
        }
        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            // revert UI: ensure adapter marks as not-favorite if server cannot be contacted
            if (adapter != null) adapter.updateFavorite(product.getId(), false);
            return;
        }

        apiProfile profileApi = ApiService.getApi(requireContext()).create(apiProfile.class);

        if (wasFavorite) {
            // remove
            profileApi.removeFromWishlist(product.getId()).enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (!response.isSuccessful()) {
                        // revert UI
                        if (adapter != null) adapter.updateFavorite(product.getId(), true);
                        Toast.makeText(getContext(), "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    // revert UI
                    if (adapter != null) adapter.updateFavorite(product.getId(), true);
//                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // add
            profileApi.addToWishlist(product.getId()).enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (!response.isSuccessful()) {
                        // revert UI
                        if (adapter != null) adapter.updateFavorite(product.getId(), false);
                        Toast.makeText(getContext(), "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    // revert UI
                    if (adapter != null) adapter.updateFavorite(product.getId(), false);
//                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}