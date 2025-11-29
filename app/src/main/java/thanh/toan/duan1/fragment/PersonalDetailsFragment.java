package thanh.toan.duan1.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thanh.toan.duan1.R;
import thanh.toan.duan1.api.ApiService;
import thanh.toan.duan1.api.apiProfile;
import thanh.toan.duan1.model.User;

public class PersonalDetailsFragment extends Fragment {

    private TextInputEditText etFullName, etPhone, etAddress;
    private MaterialButton btnSave, btnBack;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the full-screen fragment layout
        View view = inflater.inflate(R.layout.fragment_personal_details, container, false);
        etFullName = view.findViewById(R.id.et_full_name);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        btnSave = view.findViewById(R.id.btn_save);
        btnBack = view.findViewById(R.id.btn_back);

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);

        loadCurrentProfile();
        setupActions();
        return view;
    }

    private void loadCurrentProfile() {
        if (token == null) return;
        apiProfile api = ApiService.getApi(requireContext()).create(apiProfile.class);
        api.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User u = response.body();
                    if (u.getFullName() != null) etFullName.setText(u.getFullName());
                    if (u.getPhone() != null) etPhone.setText(u.getPhone());
                    if (u.getAddress() != null) etAddress.setText(u.getAddress());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnSave.setOnClickListener(v -> {
            if (token == null) {
                Toast.makeText(getContext(), "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            String fullName = textOrEmpty(etFullName);
            String phone = textOrEmpty(etPhone);
            String address = textOrEmpty(etAddress);

            Map<String, Object> data = new HashMap<>();
            if (!fullName.isEmpty()) data.put("fullName", fullName);
            if (!phone.isEmpty()) data.put("phone", phone);
            if (!address.isEmpty()) data.put("address", address);

            apiProfile api = ApiService.getApi(requireContext()).create(apiProfile.class);
            api.updateUserProfile("Bearer " + token, data).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(getContext(), "Không thể cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String textOrEmpty(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
