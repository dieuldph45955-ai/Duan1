package thanh.toan.duan1.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import thanh.toan.duan1.model.User;

public interface apiProfile {
    // get profile
    @GET("users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    // Thêm sản phẩm vào wishlist
    @POST("users/wishlist/{productId}")
    Call<User> addToWishlist(
            @Path("productId") String productId,
            @Header("Authorization") String token
    );

    // Xóa sản phẩm khỏi wishlist (backend có /api/ trong path)
    @DELETE("api/users/wishlist/{productId}")
    Call<User> removeFromWishlist(
            @Path("productId") String productId,
            @Header("Authorization") String token
    );
}
