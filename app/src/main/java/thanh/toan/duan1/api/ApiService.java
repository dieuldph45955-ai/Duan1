package thanh.toan.duan1.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {
    private static Retrofit retrofit;
    public static final String BASE_URL = "http://10.0.2.2:3000/api/";

    public static Retrofit getApi(Context context) {
        if (retrofit == null) {
            // Cấu hình Gson để parse Date
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    if (!token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    Request request = builder.method(original.method(), original.body()).build();
                    return chain.proceed(request);
                })
                .build();
    }
}
