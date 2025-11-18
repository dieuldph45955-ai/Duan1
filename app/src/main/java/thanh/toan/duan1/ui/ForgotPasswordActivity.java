package thanh.toan.duan1.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editEmail, editNewPassword;
    private Button btnResetPassword, btnSubmitNewPassword, btnBack;
    private TextView tvLogin;
    private ApiLogin api;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editEmail = findViewById(R.id.editTextEmail);
        editNewPassword = findViewById(R.id.editTextNewPassword);
        btnResetPassword = findViewById(R.id.buttonResetPassword);
        btnSubmitNewPassword = findViewById(R.id.buttonSubmitNewPassword); // FIX QUAN TRỌNG
        btnBack = findViewById(R.id.buttonBack);
        tvLogin = findViewById(R.id.textViewLogin);

        api = ApiService.getClient(this).create(ApiLogin.class);


        btnResetPassword.setOnClickListener(v -> initiateResetPassword());
        btnSubmitNewPassword.setOnClickListener(v -> submitNewPassword());
        btnBack.setOnClickListener(v -> finish());
        tvLogin.setOnClickListener(v -> finish());


        editNewPassword.setVisibility(View.GONE);
        btnSubmitNewPassword.setVisibility(View.GONE);
    }


    private void initiateResetPassword() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        api.forgotPassword(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {

                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email valid. Now enter your new password.",
                            Toast.LENGTH_SHORT).show();


                    editNewPassword.setVisibility(View.VISIBLE);
                    btnSubmitNewPassword.setVisibility(View.VISIBLE);

                    btnResetPassword.setVisibility(View.GONE);

                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email not found!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void submitNewPassword() {
        String email = editEmail.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("newPassword", newPassword);

        api.resetPassword(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Password reset successfully!",
                            Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Error resetting password!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
