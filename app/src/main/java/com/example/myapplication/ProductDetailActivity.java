package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.myapplication.Utils.DatabaseHelper;
import com.example.myapplication.Models.Product;

public class ProductDetailActivity extends AppCompatActivity {
    // Sẽ được triển khai ở chức năng tiếp theo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
    }
}

