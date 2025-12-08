package thanh.toan.duan1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import thanh.toan.duan1.R;

public class OrderSuccessActivity extends AppCompatActivity {

    private Button btnViewOrders, btnBackHome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        btnViewOrders = findViewById(R.id.btn_view_orders);
        btnBackHome = findViewById(R.id.btn_back_home);

        // If caller requested to open orders after success, show success screen briefly then open MyOrdersActivity
        boolean openOrders = getIntent().getBooleanExtra("openOrders", false);
        final String createdOrderId = getIntent().getStringExtra("orderId");
        if (openOrders) {
            // show for 1.5 seconds then navigate to MyOrdersActivity
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(OrderSuccessActivity.this, MyOrdersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (createdOrderId != null) intent.putExtra("orderId", createdOrderId);
                startActivity(intent);
                finish();
            }, 1500);
            return; // keep the success UI visible until handler runs
        }

        btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MyOrdersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (createdOrderId != null) intent.putExtra("orderId", createdOrderId);
            startActivity(intent);
            finish();
        });

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}