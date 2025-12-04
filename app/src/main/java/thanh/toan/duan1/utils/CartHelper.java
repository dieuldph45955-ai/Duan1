package thanh.toan.duan1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiCart;
import thanh.toan.duan1.model.Product;

/**
 * Lightweight helper that tries to sync add-to-cart with server when user is logged in.
 * Falls back to local SharedPreferences cart if network/user not available.
 */
public class CartHelper {
    public static void addToCart(Context context, Product product, int quantity) {
        if (product == null || product.getId() == null) return;
        SharedPreferences pref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        if (token != null && !token.isEmpty()) {
            apiCart api = ApiService.getApi(context).create(apiCart.class);
            Map<String, Object> body = new HashMap<>();
            body.put("productId", product.getId());
            body.put("quantity", quantity);
            String bearer = "Bearer " + token;
            api.addToCart(bearer, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã thêm vào giỏ hàng (đồng bộ server)", Toast.LENGTH_SHORT).show();
                        // keep local cart in sync for UI
                        try {
                            CartManager cm = new CartManager(context);
                            cm.addToCart(product, quantity);
                        } catch (Exception ignored) {}
                    } else {
                        Toast.makeText(context, "Không thể thêm vào giỏ hàng trên server, lưu cục bộ", Toast.LENGTH_SHORT).show();
                        try { new CartManager(context).addToCart(product, quantity); } catch (Exception ignored) {}
                        Log.e("CartHelper", "Server addToCart failed code=" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(context, "Lỗi kết nối khi thêm giỏ hàng, lưu cục bộ", Toast.LENGTH_SHORT).show();
                    try { new CartManager(context).addToCart(product, quantity); } catch (Exception ignored) {}
                    Log.e("CartHelper", "addToCart network failure", t);
                }
            });
        } else {
            // not logged in -> local only
            try {
                new CartManager(context).addToCart(product, quantity);
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Lỗi thêm vào giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

