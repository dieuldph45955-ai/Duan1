package thanh.toan.duan1.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiLogin;
import thanh.toan.duan1.api.ApiService;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;
    private CheckBox cbRemember;
    private ApiLogin api;

    private static final String PREFS_NAME = "MyAppPrefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvRegister = findViewById(R.id.textViewRegister);
        tvForgot = findViewById(R.id.textViewForgotPassword);
        cbRemember = findViewById(R.id.cbRemember); // cần thêm checkbox Remember trong layout

        api = ApiService.getApi(this).create(ApiLogin.class);

        // Load tài khoản đã lưu
        loadSavedAccount();

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> register());
        tvForgot.setOnClickListener(v -> forgotPassword());
    }

    private void loadSavedAccount() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = pref.getString("email", "");
        String savedPassword = pref.getString("password", "");
        boolean remember = pref.getBoolean("remember", false);

        editEmail.setText(savedEmail);
        editPassword.setText(savedPassword);
        cbRemember.setChecked(remember);
    }

    private void forgotPassword() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    private void register() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> res = response.body();

                String token = res.get("token") != null ? res.get("token").toString() : "";

                Map<?, ?> userMap = null;
                if (res.get("user") instanceof Map) {
                    userMap = (Map<?, ?>) res.get("user");
                }

                if (userMap == null) {
                    Toast.makeText(LoginActivity.this, "User data invalid!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String username = userMap.get("username") != null ? userMap.get("username").toString() : "";
                Boolean isAdmin = userMap.get("isAdmin") instanceof Boolean ? (Boolean) userMap.get("isAdmin") : false;

                // Lưu token + thông tin người dùng
                SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("token", token);
                editor.putString("username", username);
                editor.putBoolean("isAdmin", isAdmin);

                // Lưu email/password nếu chọn Remember
                if (cbRemember.isChecked()) {
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.putBoolean("remember", true);
                } else {
                    editor.remove("email");
                    editor.remove("password");
                    editor.putBoolean("remember", false);
                }

                editor.apply();

                Toast.makeText(LoginActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
