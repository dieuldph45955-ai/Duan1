package thanh.toan.duan1.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import thanh.toan.duan1.ui.LoginActivity;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView, userEmailTextView;
    private Button logoutButton;
    private MaterialButton btnHistory, btnPersonalDetails, btnPaymentMethod, btnSaveDetails;
    private MaterialCardView cardPersonalDetails;
    private TextInputEditText etFullName, etPassword;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userNameTextView = view.findViewById(R.id.user_name_text_view);
        userEmailTextView = view.findViewById(R.id.user_email_text_view);
        logoutButton = view.findViewById(R.id.logout_button);
        btnHistory = view.findViewById(R.id.btn_history);
        btnPersonalDetails = view.findViewById(R.id.btn_personal_details);
        btnPaymentMethod = view.findViewById(R.id.btn_payment_method);
        cardPersonalDetails = view.findViewById(R.id.card_personal_details);
        etFullName = view.findViewById(R.id.et_full_name);
        etPassword = view.findViewById(R.id.et_password);
        btnSaveDetails = view.findViewById(R.id.btn_save_details);

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", null);

        loadUserInfo();
        setupActions();
        setupLogout();

        return view;
    }

    private void setupActions() {
        btnPersonalDetails.setOnClickListener(v -> {
            // Open full-screen PersonalDetailsFragment
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new PersonalDetailsFragment());
            ft.addToBackStack(null);
            ft.commit();
        });

        btnSaveDetails.setOnClickListener(v -> {
            // This button is no longer used if you navigate to separate screen; optionally hide or remove.
        });

        btnHistory.setOnClickListener(v -> {
            // Open MyOrdersActivity to show user's order history
            try {
                Intent intent = new Intent(requireContext(), thanh.toan.duan1.ui.MyOrdersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không thể mở lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });

        btnPaymentMethod.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Payment method coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserInfo() {
        apiProfile api = ApiService.getApi(requireContext()).create(apiProfile.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String tk = prefs.getString("token", null);
        if (tk == null || tk.isEmpty()) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<User> call = api.getUserProfile("Bearer " + tk);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    userNameTextView.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
                    userEmailTextView.setText(user.getEmail() != null ? user.getEmail() : "");
                } else {
                    Toast.makeText(getContext(), "Không tải được thông tin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLogout() {
        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().remove("token").apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
    }
}
