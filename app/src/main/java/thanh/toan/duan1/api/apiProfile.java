package thanh.toan.duan1.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import thanh.toan.duan1.model.Product;
import thanh.toan.duan1.model.User;

public interface apiProfile {
    // get profile
    @GET("users/profile")
    Call<User> getUserProfile();

    // raw profile (returns either User or { message, user })
    @GET("users/profile")
    Call<Object> getUserProfileRaw();

    // Thêm sản phẩm vào wishlist
    @POST("users/wishlist/{productId}")
    Call<User> addToWishlist(
            @Path("productId") String productId
    );

    // Xóa sản phẩm khỏi wishlist (đã sửa đường dẫn cho đúng với backend)
    @DELETE("users/wishlist/{productId}")
    Call<User> removeFromWishlist(
            @Path("productId") String productId
    );

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") String id);

    // Cập nhật thông tin người dùng
    @PUT("users/profile")
    Call<Object> updateUserProfile(
            @Body Map<String, Object> updateData
    );
}
