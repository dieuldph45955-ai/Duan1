package thanh.toan.duan1.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import thanh.toan.duan1.request.WishlistResponse;

public interface apiFavorite {

    // All wishlist endpoints require Authorization header (Bearer token)
    @GET("users/wishlist")
    Call<WishlistResponse> getWishlist(@Header("Authorization") String authorization);

    @POST("users/wishlist/{productId}")
    Call<WishlistResponse> addToWishlist(
            @Header("Authorization") String authorization,
            @Path("productId") String productId
    );

    @DELETE("users/wishlist/{productId}")
    Call<WishlistResponse> removeFromWishlist(
            @Header("Authorization") String authorization,
            @Path("productId") String productId
    );

    @DELETE("users/wishlist/clear/all")
    Call<WishlistResponse> clearAllWishlist(@Header("Authorization") String authorization);
}
