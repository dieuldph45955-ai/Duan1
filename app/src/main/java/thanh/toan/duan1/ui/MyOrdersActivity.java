package thanh.toan.duan1.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.adapter.OrderAdapter;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiOrder;
import thanh.toan.duan1.model.Order;

public class MyOrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private apiOrder api;
    private TextView tvEmpty;
    private Button btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        // Ánh xạ view
        recyclerView = findViewById(R.id.orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Khởi tạo adapter với listener (this)
        adapter = new OrderAdapter(this, orderList, this);
        recyclerView.setAdapter(adapter);

        api = ApiService.getApi(this).create(apiOrder.class);
        
        btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Navigate back to MainActivity and open Profile (Settings)
                android.content.Intent intent = new android.content.Intent(MyOrdersActivity.this, MainActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("openProfile", true);
                startActivity(intent);
                finish();
            });
        }

        // If activity was started with an orderId (optional), we can scroll to it later
        String startedOrderId = getIntent().getStringExtra("orderId");
        if (startedOrderId != null) {
            // optionally store and scroll after fetch
        }

        fetchMyOrders();
    }

    private void fetchMyOrders() {
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // show loading state (optional TextView if available)
        if (tvEmpty != null) tvEmpty.setText("Đang tải...");

        api.getMyOrders("Bearer " + token).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();

                    // Lọc bỏ các đơn hàng đã xóa local
                    Set<String> deletedIds = getDeletedOrders();
                    for (Order order : response.body()) {
                        if (order == null) continue;
                        if (!deletedIds.contains(order.getID())) {
                            orderList.add(order);
                        }
                    }

                    // Đảo ngược danh sách để đơn mới nhất lên đầu
                    Collections.reverse(orderList);

                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        if (tvEmpty != null) tvEmpty.setText("Bạn chưa có đơn hàng nào");
                        else Toast.makeText(MyOrdersActivity.this, "Bạn chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
                    } else {
                        if (tvEmpty != null) tvEmpty.setText("");
                    }

                } else {
                    // handle HTTP errors
                    if (response.code() == 401) {
                        // unauthorized - force login
                        Toast.makeText(MyOrdersActivity.this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                        startActivity(new android.content.Intent(MyOrdersActivity.this, thanh.toan.duan1.ui.LoginActivity.class));
                        finish();
                        return;
                    }

                    String errorBody = "";
                    try (ResponseBody err = response.errorBody()) {
                        if (err != null) errorBody = err.string();
                    } catch (Exception e) {
                        // ignore
                    }

                    String msg = "Không tải được danh sách đơn hàng: " + response.code();
                    if (!errorBody.isEmpty()) msg += "\n" + errorBody;

                    new androidx.appcompat.app.AlertDialog.Builder(MyOrdersActivity.this)
                            .setTitle("Lỗi")
                            .setMessage(msg)
                            .setPositiveButton("Thử lại", (d, w) -> fetchMyOrders())
                            .setNegativeButton("Đóng", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                // network error - show retry option
                if (tvEmpty != null) tvEmpty.setText("");
                new androidx.appcompat.app.AlertDialog.Builder(MyOrdersActivity.this)
                        .setTitle("Lỗi kết nối")
                        .setMessage("Không thể kết nối tới server: " + t.getMessage())
                        .setPositiveButton("Thử lại", (d, w) -> fetchMyOrders())
                        .setNegativeButton("Đóng", (d, w) -> {
                            // optionally finish activity
                        })
                        .show();
            }
        });
    }

    @Override
    public void onCancelOrder(Order order) {
        // Danh sách lý do hủy
        final String[] reasons = {
                "Muốn thay đổi địa chỉ/số điện thoại nhận hàng",
                "Muốn thay đổi sản phẩm trong đơn hàng",
                "Thủ tục thanh toán quá rắc rối",
                "Tìm thấy giá rẻ hơn ở nơi khác",
                "Đổi ý, không muốn mua nữa",
                "Lý do khác"
        };

        // Mặc định chọn lý do đầu tiên
        final int[] selectedPosition = {0};

        new AlertDialog.Builder(this)
                .setTitle("Chọn lý do hủy đơn hàng")
                .setSingleChoiceItems(reasons, 0, (dialog, which) -> {
                    selectedPosition[0] = which;
                })
                .setPositiveButton("Xác nhận hủy", (dialog, which) -> {
                    String reason = reasons[selectedPosition[0]];
                    cancelOrder(order, reason);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void cancelOrder(Order order, String reason) {
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        Log.d("CancelOrder", "Cancelling order " + order.getID() + " with reason: " + reason);

        // Tạo object Order chứa status mới để gửi lên server
        Order statusUpdate = new Order();
        statusUpdate.setStatus("Cancelled");

        api.updateOrderStatus("Bearer " + token, order.getID(), statusUpdate).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyOrdersActivity.this, "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    fetchMyOrders(); // Tải lại danh sách sau khi hủy
                } else {
                    Toast.makeText(MyOrdersActivity.this, "Không thể hủy đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(MyOrdersActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteOrder(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa đơn hàng")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này khỏi lịch sử không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteOrder(order))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteOrder(Order order) {
        // Xử lý xóa local vì server trả về 404 (endpoint không tồn tại hoặc lỗi)
        saveDeletedOrder(order.getID());
        
        // Cập nhật UI ngay lập tức
        orderList.remove(order);
        adapter.notifyDataSetChanged();
        
        Toast.makeText(MyOrdersActivity.this, "Đã xóa đơn hàng khỏi danh sách", Toast.LENGTH_SHORT).show();
        
        // Vẫn thử gọi API để nếu server sửa lỗi thì sẽ hoạt động, nhưng không chặn UI bằng callback
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");
        api.deleteOrder("Bearer " + token, order.getID()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Log kết quả để debug nhưng không cần báo lỗi cho user nữa vì đã xóa local rồi
                if (!response.isSuccessful()) {
                    Log.w("DeleteOrder", "Server delete failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w("DeleteOrder", "Server delete error: " + t.getMessage());
            }
        });
    }

    // Lưu ID đơn hàng đã xóa vào SharedPreferences
    private void saveDeletedOrder(String orderId) {
        SharedPreferences pref = getSharedPreferences("DeletedOrders", Context.MODE_PRIVATE);
        Set<String> deletedIds = pref.getStringSet("ids", new HashSet<>());
        Set<String> newDeletedIds = new HashSet<>(deletedIds);
        newDeletedIds.add(orderId);
        pref.edit().putStringSet("ids", newDeletedIds).apply();
    }

    // Lấy danh sách ID đơn hàng đã xóa
    private Set<String> getDeletedOrders() {
        SharedPreferences pref = getSharedPreferences("DeletedOrders", Context.MODE_PRIVATE);
        return pref.getStringSet("ids", new HashSet<>());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders each time activity becomes visible
        fetchMyOrders();
    }

}
