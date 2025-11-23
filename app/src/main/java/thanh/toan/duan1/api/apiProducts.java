package thanh.toan.duan1.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Header;
import thanh.toan.duan1.model.Product;

public interface apiProducts {

    // Lấy tất cả sản phẩm (tìm kiếm + lọc)
    @GET("getAllProducts")
    Call<List<Product>> getAllProducts(
            @Query("category") String category,
            @Query("search") String search
    );

    // Lấy chi tiết sản phẩm theo ID
    @GET("products/{id}")
    Call<Product> getProductDetail(@Path("id") String productId);

    // Thêm sản phẩm mới (Admin)
    @POST("products")
    Call<Product> createProduct(
            @Header("Authorization") String token,
            @Body Product product
    );

    // Cập nhật sản phẩm (Admin)
    @PUT("products/{id}")
    Call<Product> updateProduct(
            @Path("id") String productId,
            @Header("Authorization") String token,
            @Body Product product
    );

    // Xoá sản phẩm (Admin)
    @DELETE("products/{id}")
    Call<Map<String, String>> deleteProduct(
            @Path("id") String productId,
            @Header("Authorization") String token
    );

    // Lấy sản phẩm theo danh mục
    @GET("getAllProducts")
    Call<List<Product>> getProductsByCategory(
            @Query("category") String category
    );


}