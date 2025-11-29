package thanh.toan.duan1.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiOrder;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.Item;
import thanh.toan.duan1.model.OrderCreateResponse;
import thanh.toan.duan1.model.User;
import thanh.toan.duan1.request.OrderRequest;
import thanh.toan.duan1.utils.CartManager;

public class CheckoutActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etAddress;
    private RadioGroup radioGroupPayment;
    private TextView tvTotal;
    private Button btnConfirm;
    private CartManager cartManager;
    private List<Item> cartItems;
    private apiOrder apiOrderService;
    private apiProfile apiProfileService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();

        cartManager = new CartManager(this);
        cartItems = cartManager.getCart();
        apiOrderService = ApiService.getApi(this).create(apiOrder.class);
        apiProfileService = ApiService.getApi(this).create(apiProfile.class);

        calculateTotal();
        loadUserInfo(); // Tự động điền thông tin người dùng

        btnConfirm.setOnClickListener(v -> confirmCheckout());
    }

    private void initViews() {
        etName = findViewById(R.id.et_checkout_name);
        etPhone = findViewById(R.id.et_checkout_phone);
        etAddress = findViewById(R.id.et_checkout_address);
        radioGroupPayment = findViewById(R.id.radio_group_payment);
        tvTotal = findViewById(R.id.tv_checkout_total);
        btnConfirm = findViewById(R.id.btn_confirm_order);
    }

    private void loadUserInfo() {
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");
        if (token.isEmpty()) return;

        apiProfileService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user.getFullName() != null) etName.setText(user.getFullName());
                    else if (user.getUsername() != null) etName.setText(user.getUsername());
                    
                    if (user.getPhone() != null) etPhone.setText(user.getPhone());
                    if (user.getAddress() != null) etAddress.setText(user.getAddress());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Không làm gì nếu lỗi load profile, để người dùng tự nhập
            }
        });
    }

    private void calculateTotal() {
        double total = 0;
        for (Item item : cartItems) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotal.setText(nf.format(total) + " VND");
    }

    private void confirmCheckout() {
        if (etName.getText() == null || etPhone.getText() == null || etAddress.getText() == null) {
             Toast.makeText(this, "Lỗi giao diện nhập liệu", Toast.LENGTH_SHORT).show();
             return;
        }

        String name = Objects.requireNonNull(etName.getText()).toString();
        String phone = Objects.requireNonNull(etPhone.getText()).toString();
        String address = Objects.requireNonNull(etAddress.getText()).toString();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();
        String paymentMethodName = "Thanh toán khi nhận hàng (COD)";
        if (selectedPaymentId == R.id.rb_banking) {
            paymentMethodName = "Chuyển khoản ngân hàng";
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt hàng với phương thức: " + paymentMethodName + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> processCheckout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processCheckout() {
        String name = Objects.requireNonNull(etName.getText()).toString();
        String phone = Objects.requireNonNull(etPhone.getText()).toString();
        String address = Objects.requireNonNull(etAddress.getText()).toString();

        int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();
        String paymentMethod = "COD";
        if (selectedPaymentId == R.id.rb_banking) {
            paymentMethod = "BANKING";
        }

        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItemRequest> orderItems = new ArrayList<>();
        for (Item item : cartItems) {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                orderItems.add(new OrderRequest.OrderItemRequest(item.getProduct().getId(), item.getQuantity().intValue()));
            }
        }

        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống hoặc lỗi sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderRequest orderRequest = new OrderRequest(orderItems, address, phone, paymentMethod, true);

        apiOrderService.createOrder("Bearer " + token, orderRequest).enqueue(new Callback<OrderCreateResponse>() {
            @Override
            public void onResponse(Call<OrderCreateResponse> call, Response<OrderCreateResponse> response) {
                if (response.isSuccessful()) {
                    // Xóa giỏ hàng sau khi đặt thành công
                    cartManager.clearCart();
                    Log.d("Checkout", "Order success, navigating to success screen");

                    // Start OrderSuccessActivity and ask it to auto-open MyOrders
                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (response.body() != null && response.body().getId() != null) {
                        intent.putExtra("orderId", response.body().getId());
                    }
                    intent.putExtra("openOrders", true);
                    startActivity(intent);
                    finish();

                } else {
                    String errorBody = "";
                    try (ResponseBody errorResponse = response.errorBody()) {
                        if (errorResponse != null) {
                            errorBody = errorResponse.string();
                        }
                    } catch (IOException e) {
                        Log.e("CheckoutError", "Error reading error body", e);
                    }
                    
                    Log.e("CheckoutError", "Code: " + response.code() + ", Body: " + errorBody);
                    
                    new AlertDialog.Builder(CheckoutActivity.this)
                            .setTitle("Đặt hàng thất bại")
                            .setMessage("Lỗi server: " + response.code() + "\n" + errorBody)
                            .setPositiveButton("Đóng", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<OrderCreateResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckoutError", "Network error", t);
            }
        });
    }
}
