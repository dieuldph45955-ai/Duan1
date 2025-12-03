package thanh.toan.duan1.api;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import thanh.toan.duan1.model.Order;
import thanh.toan.duan1.model.OrderCreateResponse;
import thanh.toan.duan1.request.OrderRequest;

public interface apiOrder {
    // Tạo đơn hàng - sử dụng OrderCreateResponse để tránh lỗi parse JSON
    @POST("orders")
    Call<OrderCreateResponse> createOrder(@Header("Authorization") String token, @Body OrderRequest body);

    // Lấy đơn hàng của user
    @GET("orders/my-orders")
    Call<List<Order>> getMyOrders(@Header("Authorization") String token);

    // Lấy tất cả đơn hàng (admin)
    @GET("ordersAdmin")
    Call<List<Order>> getAllOrdersAdmin(@Header("Authorization") String token);

    // Cập nhật trạng thái đơn hàng (Admin/User Cancel) - accept Map to send { status: "..." }
    @PUT("orders/{id}/status")
    Call<Order> updateOrderStatus(@Header("Authorization") String token, @Path("id") String orderId, @Body Map<String, String> statusBody);

    // Xóa đơn hàng (chỉ dành cho đơn đã hủy)
    @DELETE("orders/{id}")
    Call<ResponseBody> deleteOrder(@Header("Authorization") String token, @Path("id") String orderId);
}
