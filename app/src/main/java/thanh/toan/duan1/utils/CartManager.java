package thanh.toan.duan1.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import thanh.toan.duan1.model.Item;
import thanh.toan.duan1.model.Product;

public class CartManager {
    private static final String PREF_NAME = "CartPrefs";
    private static final String KEY_CART = "cart_items";
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public CartManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<Item> getCart() {
        String json = prefs.getString(KEY_CART, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Item>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addToCart(Product product, int quantity) {
        List<Item> cart = getCart();
        boolean exists = false;
        for (Item item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }
        if (!exists) {
            Item newItem = new Item();
            newItem.setProduct(product);
            newItem.setQuantity((long)quantity);
            cart.add(newItem);
        }
        saveCart(cart);
    }

    public void updateQuantity(int position, int quantity) {
        List<Item> cart = getCart();
        if (position >= 0 && position < cart.size()) {
            cart.get(position).setQuantity((long) quantity);
            saveCart(cart);
        }
    }

    public void removeFromCart(int position) {
        List<Item> cart = getCart();
        if (position >= 0 && position < cart.size()) {
            cart.remove(position);
            saveCart(cart);
        }
    }

    public void clearCart() {
        prefs.edit().remove(KEY_CART).apply();
    }

    private void saveCart(List<Item> cart) {
        String json = gson.toJson(cart);
        prefs.edit().putString(KEY_CART, json).apply();
    }
}

