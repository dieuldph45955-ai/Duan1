package thanh.toan.duan1.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import thanh.toan.duan1.model.Item;
import thanh.toan.duan1.model.OrderCreateResponse;
import thanh.toan.duan1.request.OrderRequest;

public interface apiCart {

    // Lấy danh sách giỏ hàng
    @GET("cart")
    Call<List<Item>> getCart(@Header("Authorization") String token);

    // Thêm sản phẩm vào giỏ hàng
    @POST("cart/add")
    Call<Map<String, Object>> addToCart(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );

    // Cập nhật số lượng sản phẩm trong giỏ hàng (returns updated Item)
    @PUT("cart/update/{itemId}")
    Call<Item> updateCartItem(
            @Header("Authorization") String token,
            @Path("itemId") String itemId,
            @Body Map<String, Object> body
    );

    // Xóa sản phẩm khỏi giỏ hàng
    @DELETE("cart/remove/{itemId}")
    Call<Void> removeCartItem(
            @Header("Authorization") String token,
            @Path("itemId") String itemId
    );
    
    // Checkout (tạo đơn hàng từ giỏ hàng) - gửi OrderRequest và nhận OrderCreateResponse
    @POST("orders")
    Call<OrderCreateResponse> checkout(@Header("Authorization") String token, @Body OrderRequest body);
}
