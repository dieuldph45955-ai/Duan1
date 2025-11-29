package thanh.toan.duan1.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import thanh.toan.duan1.request.WishlistResponse;

public interface apiFavorite {

    @GET("wishlist")
    Call<WishlistResponse> getWishlist(String s);

    @POST("wishlist/{productId}")
    Call<WishlistResponse> addToWishlist(
            @Path("productId") String productId
    );

    @DELETE("wishlist/{productId}")
    Call<WishlistResponse> removeFromWishlist(

            @Path("productId") String productId
    );

    @DELETE("wishlist/clear/all")
    Call<WishlistResponse> clearAllWishlist();
}
