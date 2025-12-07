package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.User;
import com.example.cloudstorage.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class my_profiile extends AppCompatActivity {

    private TextView userEmailView;
    private TextView userNameView;
    private TextView userBioView;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profiile);

        // Khởi tạo TokenManager
        tokenManager = new TokenManager(this);

        // Kiểm tra đăng nhập
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Tìm views
        userEmailView = findViewById(R.id.user_email_view);
        userNameView = findViewById(R.id.user_name_view_detail);
        userBioView = findViewById(R.id.user_bio_view);

        ImageView btn_back = findViewById(R.id.btn_back);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Load user profile từ API
        loadUserProfile();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Gọi API /auth/me để lấy thông tin user
     */
    private void loadUserProfile() {
        // Authorization header sẽ tự động được thêm bởi AuthInterceptor
        ApiClient.getApiService(this).getProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();

                    // Hiển thị thông tin user
                    displayUserInfo(user);

                } else {
                    Toast.makeText(my_profiile.this, "Không thể tải thông tin user", Toast.LENGTH_SHORT).show();

                    // Nếu lỗi 401 (Unauthorized), chuyển về login
                    if (response.code() == 401) {
                        tokenManager.clearToken();
                        navigateToLogin();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(my_profiile.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    /**
     * Hiển thị thông tin user lên UI
     */
    private void displayUserInfo(User user) {
        if (userEmailView != null) {
            userEmailView.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        }
        if (userNameView != null) {
            userNameView.setText(user.getName() != null ? user.getName() : "N/A");
        }
        if (userBioView != null) {
            userBioView.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "Chưa có tiểu sử");
        }
    }

    /**
     * Chuyển về màn hình login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(my_profiile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}