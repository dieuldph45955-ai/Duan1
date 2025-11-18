package thanh.toan.duan1.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiLogin {
    // Đăng ký
    @POST("register")
    Call<Map<String, Object>> register(@Body Map<String, Object> body);

    // Đăng nhập
    @POST("login")
    Call<Map<String, Object>> login(@Body Map<String, Object> body);

    // Quên mật khẩu
    @POST("forgot-password")
    Call<Map<String, Object>> forgotPassword(@Body Map<String, Object> body);
    @POST("reset-password") //  reset mật khẩu
    Call<Map<String, Object>> resetPassword(@Body Map<String, Object> body);
}
