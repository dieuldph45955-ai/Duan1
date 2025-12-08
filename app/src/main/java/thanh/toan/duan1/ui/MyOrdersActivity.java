package thanh.toan.duan1.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private ImageButton btnBack;

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
                // Navigate back to MainActivity and open Home (default)
                android.content.Intent intent = new android.content.Intent(MyOrdersActivity.this, MainActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                // Do not request opening profile; default MainActivity will show HomeFragment
                // If you need to programmatically select Home tab, we could add an extra like openHome, but
                // MainActivity defaults to Home when no extras are provided.
                startActivity(intent);
                finish();
            });
        }

        // If activity was started with an orderId (optional), we can scroll to it later
        String startedOrderId = getIntent().getStringExtra("orderId");
        if (startedOrderId != null) {
            // nothing to do now; we'll normalize after fetch when comparing
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
                        String rawCode = order.getCode();
                        String displayCode = order.getDisplayCode();
                        String oid = order.getID();
                        boolean isDeleted = false;
                        // check id, raw code, and display code (exact matches)
                        if (oid != null && deletedIds.contains(oid)) isDeleted = true;
                        if (rawCode != null && deletedIds.contains(rawCode)) isDeleted = true;
                        if (displayCode != null && deletedIds.contains(displayCode)) isDeleted = true;
                        // fallback: short-code variant
                        String shortc = normalizeShortCode(rawCode, oid);
                        if (shortc != null && deletedIds.contains(shortc)) isDeleted = true;
                        if (!isDeleted) orderList.add(order);
                    }

                    // Đảo ngược danh sách để đơn mới nhất lên đầu
                    // server may not guarantee order; ensure the created order (if provided via intent) is first
                    String createdOrderId = getIntent().getStringExtra("orderId");
                    String createdOrderIdNorm = createdOrderId != null ? createdOrderId.toLowerCase(java.util.Locale.ROOT) : null;
                    if (createdOrderId != null) {
                         // find the order and move it to the front
                         int found = -1;
                         for (int i = 0; i < orderList.size(); i++) {
                             Order o = orderList.get(i);
                             if (o == null) continue;
                             String oid = o.getID();
                             String ocode = o.getCode();
                             String display = o.getDisplayCode();
                             // match by raw code or id or short-code
                            String normalizedO = normalizeShortCode(ocode, oid);
                            String normalizedRequested = normalizeShortCode(createdOrderIdNorm, createdOrderIdNorm);
                            boolean matchById = (oid != null && oid.equals(createdOrderId));
                            boolean matchByCode = (ocode != null && ocode.equalsIgnoreCase(createdOrderId));
                            boolean matchByDisplay = (display != null && createdOrderIdNorm != null && display.equals(createdOrderIdNorm));
                            boolean matchByShort = (normalizedO != null && normalizedRequested != null && normalizedO.equals(normalizedRequested));
                            if (matchById || matchByCode || matchByDisplay || matchByShort) {
                                 found = i;
                                 break;
                             }
                         }
                         if (found > 0) {
                             Order created = orderList.remove(found);
                             orderList.add(0, created);
                         } else if (found == -1) {
                             // Not found in response: we keep server order and optionally you could add a placeholder or fetch the order by id.
                         }
                     } else {
                         // If no createdOrderId provided, keep newest first by reversing
                         Collections.reverse(orderList);
                     }

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

        // Send minimal payload { status: "cancelled" } (match server enum case-insensitively)
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "cancelled");

        api.updateOrderStatus("Bearer " + token, order.getID(), statusUpdate).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyOrdersActivity.this, "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    // Sau khi hủy thành công: hỏi người dùng có muốn xóa hoá đơn không
                    new androidx.appcompat.app.AlertDialog.Builder(MyOrdersActivity.this)
                            .setTitle("Đã hủy đơn hàng")
                            .setMessage("Bạn có muốn xóa hoá đơn này khỏi lịch sử không?")
                            .setPositiveButton("Xóa hoá đơn", (dialog, which) -> {
                                // Gọi API xóa
                                deleteOrder(order);
                            })
                            .setNegativeButton("Giữ lại", (dialog, which) -> {
                                // Chỉ làm mới danh sách
                                fetchMyOrders();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    String err = "Không thể hủy đơn hàng: " + response.code();
                    try (ResponseBody eb = response.errorBody()) {
                        if (eb != null) {
                            String body = eb.string();
                            if (!body.isEmpty()) err += " - " + body;
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(MyOrdersActivity.this, err, Toast.LENGTH_LONG).show();
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
        // First, attempt to delete on server. Only if server confirms delete, remove locally.
        SharedPreferences pref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        api.deleteOrder("Bearer " + token, order.getID()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // remove locally and persist that it's deleted
                    saveDeletedOrder(order.getID());
                    orderList.remove(order);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MyOrdersActivity.this, "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show();
                } else {
                    int code = response.code();
                    String serverMsg = null;
                    try (ResponseBody err = response.errorBody()) {
                        if (err != null) serverMsg = err.string();
                    } catch (Exception ignored) {}

                    if (code == 404) {
                        // not found on server — remove locally
                        saveDeletedOrder(order.getID());
                        orderList.remove(order);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MyOrdersActivity.this, "Đơn hàng không tồn tại trên server. Đã xóa local.", Toast.LENGTH_SHORT).show();
                    } else if (code == 403) {
                        Toast.makeText(MyOrdersActivity.this, "Bạn không có quyền xóa đơn này.", Toast.LENGTH_LONG).show();
                    } else if (code == 400) {
                        String msg = "Không thể xóa: " + (serverMsg != null && !serverMsg.isEmpty() ? serverMsg : "Yêu cầu không hợp lệ");
                        Toast.makeText(MyOrdersActivity.this, msg, Toast.LENGTH_LONG).show();
                    } else {
                        String msg = "Lỗi khi xóa đơn: " + code + (serverMsg != null ? (" - " + serverMsg) : "");
                        Toast.makeText(MyOrdersActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // network error — do not remove local, inform user
                Toast.makeText(MyOrdersActivity.this, "Lỗi kết nối khi xóa: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Lưu ID đơn hàng đã xóa vào SharedPreferences (lưu dạng short code)
    private void saveDeletedOrder(String orderId) {
        SharedPreferences pref = getSharedPreferences("DeletedOrders", Context.MODE_PRIVATE);
        Set<String> deletedIds = pref.getStringSet("ids", new HashSet<>());
        Set<String> newDeletedIds = new HashSet<>(deletedIds);
        if (orderId != null) {
            // Save id/code/displayCode variants for robust matching with server/admin
            newDeletedIds.add(orderId);
            // If this order object exists locally, try to derive display variant and short-code
            String shortc = normalizeShortCode(orderId, orderId);
            if (shortc != null) newDeletedIds.add(shortc);
            // Also attempt to save uppercase/lowercase variants to avoid mismatches (server may return different case)
            newDeletedIds.add(orderId.toLowerCase(java.util.Locale.ROOT));
            newDeletedIds.add(orderId.toUpperCase(java.util.Locale.ROOT));
        }
         pref.edit().putStringSet("ids", newDeletedIds).apply();
    }

    // Lấy danh sách ID đơn hàng đã xóa
    private Set<String> getDeletedOrders() {
        SharedPreferences pref = getSharedPreferences("DeletedOrders", Context.MODE_PRIVATE);
        return pref.getStringSet("ids", new HashSet<>());
    }

    private static String normalizeToNumeric(String code, String id) {
        // replaced by normalizeShortCode below; kept for parity but not used
        return normalizeShortCode(code, id);
    }

    private static String normalizeShortCode(String code, String id) {
        if (id != null && id.length() >= 8) return id.substring(0, 8).toLowerCase();
        if (code != null && code.length() >= 8) return code.substring(0, 8).toLowerCase();
        String seed = (id != null && !id.isEmpty()) ? id : (code != null ? code : "");
        int h = Math.abs(seed.hashCode());
        String hex = Integer.toHexString(h);
        if (hex.length() < 8) hex = String.format(java.util.Locale.getDefault(), "%8s", hex).replace(' ', '0');
        return hex.substring(hex.length() - 8).toLowerCase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders each time activity becomes visible
        fetchMyOrders();
    }

}
