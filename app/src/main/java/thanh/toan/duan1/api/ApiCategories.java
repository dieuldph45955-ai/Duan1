package thanh.toan.duan1.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import thanh.toan.duan1.model.Category;

public interface ApiCategories {
    @GET("categories")
    Call<List<Category>> getCategories();
}

