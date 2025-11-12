package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import java.util.UUID;
import com.example.myapplication.Utils.DatabaseHelper;
import com.example.myapplication.Models.User;

public class Dangky extends AppCompatActivity {
    EditText edtEmail, edtPassword, edtConfirm, edtName, edtPhone;
    Button btnRegister;
    TextView tvBackToLogin;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        dbHelper = new DatabaseHelper(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            
            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập Email");
                return;
            }
            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập tên");
                return;
            }
            if (pass.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }
            if (confirm.isEmpty()) {
                edtConfirm.setError("Vui lòng nhập lại mật khẩu");
                return;
            }
            if (pass.length() < 6) {
                edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }
            if (!pass.equals(confirm)) {
                edtConfirm.setError("Mật khẩu nhập lại không khớp");
                return;
            }
            
            // Tạo user mới
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setEmail(email);
            newUser.setPassword(pass);
            newUser.setName(name);
            newUser.setPhone(phone);
            newUser.setRole("user");
            newUser.setActive(true);
            
            // Đăng ký user
            if (dbHelper.registerUser(newUser)) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Dangky.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email đã tồn tại hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            }
        });
        
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Dangky.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}