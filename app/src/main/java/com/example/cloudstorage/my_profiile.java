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
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.User;
import com.example.cloudstorage.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class my_profiile extends AppCompatActivity {

    private TextView userEmailView;
    private TextView userNameView;
    private TextView userBioView;
    private TextView storageUsedView;
    private TextView filesCountView;
    private TextView foldersCountView;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profiile);

        tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        userEmailView = findViewById(R.id.user_email_view);
        userNameView = findViewById(R.id.user_name_view_detail);
        userBioView = findViewById(R.id.user_bio_view);
        storageUsedView = findViewById(R.id.storage_used_value);
        filesCountView = findViewById(R.id.files_count_value);
        foldersCountView = findViewById(R.id.folders_count_value);

        ImageView btn_back = findViewById(R.id.btn_back);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadUserProfile();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserProfile() {
        ApiClient.getApiService(this).getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    // Extract user from data field
                    if (apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        displayUserInfo(user);
                    } else {
                        Toast.makeText(my_profiile.this, "No user data", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(my_profiile.this, "Cannot load user information", Toast.LENGTH_SHORT).show();

                    if (response.code() == 401) {
                        tokenManager.clearToken();
                        navigateToLogin();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Toast.makeText(my_profiile.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void displayUserInfo(User user) {
        if (userEmailView != null) {
            userEmailView.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        }
        if (userNameView != null) {
            userNameView.setText(user.getName() != null ? user.getName() : "N/A");
        }
        if (userBioView != null) {
            userBioView.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "No bio available");
        }

        if (storageUsedView != null) {
            double totalGB = user.getTotal();
            String storageText = String.format("%.2f GB", totalGB);
            storageUsedView.setText(storageText);
        }

        if (filesCountView != null) {
            int mediaCount = user.getMediaCount();
            String filesText = formatNumber(mediaCount);
            filesCountView.setText(filesText);
        }

        if (foldersCountView != null) {
            int albumsCount = user.getAlbumsCount();
            String foldersText = formatNumber(albumsCount);
            foldersCountView.setText(foldersText);
        }
    }

    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(my_profiile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}