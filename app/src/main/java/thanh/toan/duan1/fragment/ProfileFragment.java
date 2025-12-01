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
import thanh.toan.duan1.ui.EditProfileActivity;
import thanh.toan.duan1.ui.LoginActivity;
import thanh.toan.duan1.ui.MyOrdersActivity;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView, userEmailTextView;
    private Button logoutButton, myOrdersButton;
    private static final int REQ_EDIT_PROFILE = 1001;
    private static final String TAG = "ProfileFragment";

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
        myOrdersButton = view.findViewById(R.id.btn_history);

        loadUserInfo();
        setupLogout();

        myOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyOrdersActivity.class);
            startActivity(intent);
        });

        Button editProfileBtn = view.findViewById(R.id.btn_edit_profile);
        if (editProfileBtn != null) {
            editProfileBtn.setOnClickListener(v -> {
                Intent i = new Intent(getActivity(), EditProfileActivity.class);
                startActivityForResult(i, REQ_EDIT_PROFILE);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ensure data is fresh whenever fragment resumes
        loadUserInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT_PROFILE && resultCode == getActivity().RESULT_OK) {
            loadUserInfo();
        }
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null); // KEY đúng với LoginActivity

        if (token == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiProfile api = ApiService.getApi(getContext()).create(apiProfile.class);
        Call<Object> call = api.getUserProfileRaw(); // call raw to handle different backend shapes

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                android.util.Log.d(TAG, "getUserProfileRaw: code=" + response.code() + " body=" + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    Object body = response.body();
                    // backend may return either the user object directly or { message, user }
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        // First try to convert directly to User
                        User user = null;
                        try {
                          user = gson.fromJson(gson.toJson(body), User.class);
                        } catch (Exception e) { /* ignore */ }

                        // If the parsed user has no id or username, check wrapper
                        if (user == null || (user.getId() == null && user.getUsername() == null && user.getEmail()==null)) {
                          try {
                            java.util.Map map = gson.fromJson(gson.toJson(body), java.util.Map.class);
                            if (map != null && map.get("user") != null) {
                              user = gson.fromJson(gson.toJson(map.get("user")), User.class);
                            }
                          } catch (Exception ignored) {}
                        }

                        if (user != null) {
                            android.util.Log.d(TAG, "Parsed user id=" + user.getId() + " fullName=" + user.getFullName());
                            userNameTextView.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
                            userEmailTextView.setText(user.getEmail() != null ? user.getEmail() : "");
                            return;
                        }
                    } catch (Exception ignored) {}

                    String err = "";
                    try { if (response.errorBody()!=null) err = response.errorBody().string(); } catch (Exception ignored) {}
                    android.util.Log.w(TAG, "Could not parse profile response; err="+err);
                    Toast.makeText(getContext(), "Không tải được thông tin!", Toast.LENGTH_SHORT).show();
                } else {
                    String err = "";
                    try { if (response.errorBody()!=null) err = response.errorBody().string(); } catch (Exception ignored) {}
                    android.util.Log.w(TAG, "getUserProfileRaw failed code="+response.code()+" err="+err);
                    Toast.makeText(getContext(), "Không tải được thông tin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                android.util.Log.e(TAG, "getUserProfileRaw onFailure", t);
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
