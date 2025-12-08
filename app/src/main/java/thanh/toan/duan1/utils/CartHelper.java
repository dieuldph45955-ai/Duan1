package thanh.toan.duan1.utils;

import android.content.Context;
import android.content.Intent;
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
    public static final String ACTION_CART_UPDATED = "thanh.toan.duan1.CART_UPDATED";

    public static void addToCart(Context context, Product product, int quantity) {
        if (product == null || product.getId() == null) return;
        SharedPreferences pref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");

        // Always update local cart immediately (optimistic update) so UI reflects change right away
        try {
            new CartManager(context).addToCart(product, quantity);
            // notify UI components to refresh
            Intent intent = new Intent(ACTION_CART_UPDATED);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e("CartHelper", "local addToCart failed", e);
        }

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
                        // server succeeded — keep local cart in sync but do not show duplicate toast here
                        try {
                            CartManager cm = new CartManager(context);
                            cm.addToCart(product, quantity);
                            // broadcast again to ensure UI reflects authoritative state
                            context.sendBroadcast(new Intent(ACTION_CART_UPDATED));
                        } catch (Exception ignored) {}
                    } else {
                        // server failed: we've already updated local; log for debugging
                        Log.e("CartHelper", "Server addToCart failed code=" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // network failure — we've already updated local; log for debugging
                    Log.e("CartHelper", "addToCart network failure", t);
                }
            });
        } else {
            // not logged in -> local only
            try {
                // show a simple confirmation when user is not logged in
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        }
    }
}
