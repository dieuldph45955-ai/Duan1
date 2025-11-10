package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

public class Quenmk extends AppCompatActivity {
    EditText edtEmail, edtNewPassword;
    Button btnUpdate;
    TextView tvBackLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quenmk);
        edtEmail = findViewById(R.id.edtEmail);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvBackLogin = findViewById(R.id.tvBackLogin);
        btnUpdate.setOnClickListener(v -> {
            Toast.makeText(this, "Cập nhật mật khẩu (demo)", Toast.LENGTH_SHORT).show();
        });
        tvBackLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Quenmk.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}