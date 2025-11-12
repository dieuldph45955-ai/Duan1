package com.example.myapplication.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.Models.User;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "MyAppPref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ADDRESS = "user_address";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ADDRESS, user.getAddress());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        User user = new User();
        user.setId(sharedPreferences.getString(KEY_USER_ID, null));
        user.setEmail(sharedPreferences.getString(KEY_USER_EMAIL, null));
        user.setName(sharedPreferences.getString(KEY_USER_NAME, null));
        user.setPhone(sharedPreferences.getString(KEY_USER_PHONE, null));
        user.setAddress(sharedPreferences.getString(KEY_USER_ADDRESS, null));
        user.setRole(sharedPreferences.getString(KEY_USER_ROLE, "user"));
        return user;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "user");
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
}

