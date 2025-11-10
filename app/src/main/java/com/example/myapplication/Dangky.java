package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

public class Dangky extends AppCompatActivity {
    EditText edtEmail, edtPassword, edtConfirm;
    Button btnRegister;
    TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            String confirm = edtConfirm.getText().toString().trim();
            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập Email");
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
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Dangky.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Dangky.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
    }