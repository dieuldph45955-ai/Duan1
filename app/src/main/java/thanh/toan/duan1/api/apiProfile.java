package thanh.toan.duan1.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import thanh.toan.duan1.model.User;

public interface apiProfile {
    // get profile
    @GET("users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);


}
