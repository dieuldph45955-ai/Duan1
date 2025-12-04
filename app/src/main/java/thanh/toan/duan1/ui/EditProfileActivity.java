package thanh.toan.duan1.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.User;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private EditText edtFullName, edtEmail, edtPhone, edtAddress, edtPassword;
    private Button btnSave;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile);

        edtFullName = findViewById(R.id.edt_full_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        // The backend PUT /users/profile only accepts fullName, phone, address.
        // Keep password or email editing disabled here to match server API.
        edtPassword = findViewById(R.id.edt_password);
        edtEmail.setEnabled(false); // email cannot be changed via this endpoint
        btnSave = findViewById(R.id.btn_save_profile);

        progressBar = new ProgressBar(this);
        // load current profile
        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        apiProfile api = ApiService.getApi(this).create(apiProfile.class);
        api.getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "getUserProfile: code=" + response.code() + " body=" + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    if (u.getFullName() != null) edtFullName.setText(u.getFullName());
                    if (u.getEmail() != null) edtEmail.setText(u.getEmail());
                    if (u.getPhone() != null) edtPhone.setText(u.getPhone());
                    if (u.getAddress() != null) edtAddress.setText(u.getAddress());
                } else {
                    String err = "";
                    try { if (response.errorBody()!=null) err = response.errorBody().string(); } catch (Exception ignored) {}
                    Log.w(TAG, "getUserProfile failed: code="+response.code()+" err="+err);
                    Toast.makeText(EditProfileActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "getUserProfile onFailure", t);
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        // Only validate full name; email is read-only here
        if (name.isEmpty()) {
            Toast.makeText(this, "Họ tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String newPassword = edtPassword.getText() != null ? edtPassword.getText().toString() : "";
        if (newPassword != null && !newPassword.isEmpty() && newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", name);
        data.put("phone", phone);
        data.put("address", address);
        // If user entered a new password, include it — the server should hash before saving
        if (newPassword != null && !newPassword.isEmpty()) {
            data.put("password", newPassword);
        }

        apiProfile api = ApiService.getApi(this).create(apiProfile.class);
        api.updateUserProfile(data).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.d(TAG, "updateUserProfile: code=" + response.code() + " body=" + response.body());
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    // The server returns the updated user object. We don't require it locally,
                    // just finish and let ProfileFragment reload.
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String err = "";
                    try { if (response.errorBody()!=null) err = response.errorBody().string(); } catch (Exception ignored) {}
                    Log.w(TAG, "updateUserProfile failed: code="+response.code()+" err="+err);
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "updateUserProfile onFailure", t);
                btnSave.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
