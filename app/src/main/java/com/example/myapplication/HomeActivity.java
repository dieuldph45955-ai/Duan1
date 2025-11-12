package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.myapplication.Utils.DatabaseHelper;
import com.example.myapplication.Utils.SharedPrefManager;
import com.example.myapplication.Models.Product;
import com.example.myapplication.Adapters.ProductAdapter;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private DatabaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPrefManager = new SharedPrefManager(this);
        dbHelper = new DatabaseHelper(this);

        // Kiểm tra đăng nhập
        if (!sharedPrefManager.isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trang chủ");
        }

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        
        // Hiển thị danh sách sản phẩm
        loadProducts();
    }

    private void loadProducts() {
        List<Product> productList = dbHelper.getAllProducts();
        productAdapter = new ProductAdapter(this, productList, product -> {
            // Chuyển đến trang chi tiết sản phẩm
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        // Ẩn menu admin nếu không phải admin
        if (!sharedPrefManager.getUserRole().equals("admin")) {
            menu.findItem(R.id.menu_admin).setVisible(false);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_wishlist) {
            Intent intent = new Intent(this, WishlistActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_orders) {
            Intent intent = new Intent(this, OrdersActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_admin) {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_logout) {
            sharedPrefManager.logout();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}

