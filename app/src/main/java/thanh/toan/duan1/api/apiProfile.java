package thanh.toan.duan1.api;

import retrofit2.Call;
import retrofit2.http.GET;
import thanh.toan.duan1.model.User;

public interface apiProfile {
    @GET("users/profile")
    Call<User> getUserProfile();
}

