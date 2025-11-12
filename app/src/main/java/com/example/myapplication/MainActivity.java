package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import java.util.regex.Pattern;
import com.example.myapplication.Utils.DatabaseHelper;
import com.example.myapplication.Utils.SharedPrefManager;
import com.example.myapplication.Models.User;

public class MainActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView tvRegister, tvForgot;
    DatabaseHelper dbHelper;
    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sharedPrefManager = new SharedPrefManager(this);
        
        // Kiểm tra nếu đã đăng nhập thì chuyển đến HomeActivity
        if (sharedPrefManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        dbHelper = new DatabaseHelper(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra đăng nhập
            User user = dbHelper.loginUser(email, password);
            if (user != null) {
                // Lưu thông tin user vào SharedPreferences
                sharedPrefManager.saveUser(user);
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                
                // Chuyển đến HomeActivity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Dangky.class);
            startActivity(intent);
        });

        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Quenmk.class);
            startActivity(intent);
        });
    }
    
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        return pattern.matcher(email).matches();
    }
}
