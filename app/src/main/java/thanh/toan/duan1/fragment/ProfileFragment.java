package thanh.toan.duan1.fragment;

import android.annotation.SuppressLint;
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

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userNameTextView = view.findViewById(R.id.user_name_text_view);
        userEmailTextView = view.findViewById(R.id.user_email_text_view);
        logoutButton = view.findViewById(R.id.logout_button);

        loadUserInfo();
        setupLogout();

        return view;
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null); // KEY đúng với LoginActivity

        if (token == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiProfile api = ApiService.getApi(getContext()).create(apiProfile.class);
        Call<User> call = api.getUserProfile("Bearer " + token); // Gọi method đúng

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    userNameTextView.setText(user.getUsername());
                    userEmailTextView.setText(user.getEmail());
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
            prefs.edit().remove("token").apply(); // KEY đúng

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
    }
}
