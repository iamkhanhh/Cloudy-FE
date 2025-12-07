package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.ChangePasswordRequest;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class change_password extends AppCompatActivity {

    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private Button changePasswordButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Khởi tạo TokenManager
        tokenManager = new TokenManager(this);

        // Kiểm tra đăng nhập
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Tìm views
        currentPasswordInput = findViewById(R.id.current_password_input);
        newPasswordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        changePasswordButton = findViewById(R.id.btn_change_password);
        ImageView btn_back = findViewById(R.id.btn_back);

        // Back button
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Change password button
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChangePassword();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Xử lý đổi mật khẩu
     */
    private void handleChangePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate input
        if (currentPassword.isEmpty()) {
            currentPasswordInput.setError("Vui lòng nhập mật khẩu hiện tại");
            currentPasswordInput.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordInput.setError("Vui lòng nhập mật khẩu mới");
            newPasswordInput.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            newPasswordInput.setError("Mật khẩu mới phải có ít nhất 8 ký tự");
            newPasswordInput.requestFocus();
            return;
        }

        // Validate password format (theo yêu cầu của BE)
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            newPasswordInput.setError("Mật khẩu phải chứa ít nhất 1 chữ cái, 1 số và 1 ký tự đặc biệt");
            newPasswordInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Vui lòng xác nhận mật khẩu");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mật khẩu xác nhận không khớp");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            newPasswordInput.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            newPasswordInput.requestFocus();
            return;
        }

        // Disable inputs
        setInputsEnabled(false);

        // Tạo request
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        // Gọi API
        ApiClient.getApiService(this).changePassword(request)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        setInputsEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse apiResponse = response.body();

                            Toast.makeText(change_password.this,
                                    apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đổi mật khẩu thành công!",
                                    Toast.LENGTH_SHORT).show();

                            // Clear inputs
                            currentPasswordInput.setText("");
                            newPasswordInput.setText("");
                            confirmPasswordInput.setText("");

                            // Quay lại màn hình trước hoặc logout
                            finish();

                        } else {
                            String errorMsg = "Đổi mật khẩu thất bại";

                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(change_password.this, errorMsg, Toast.LENGTH_LONG).show();

                            // Nếu lỗi 401 (Unauthorized), chuyển về login
                            if (response.code() == 401) {
                                tokenManager.clearToken();
                                navigateToLogin();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                        setInputsEnabled(true);

                        String errorMsg = "Lỗi kết nối: " + t.getMessage();
                        Toast.makeText(change_password.this, errorMsg, Toast.LENGTH_LONG).show();

                        t.printStackTrace();
                    }
                });
    }

    /**
     * Enable/disable inputs
     */
    private void setInputsEnabled(boolean enabled) {
        currentPasswordInput.setEnabled(enabled);
        newPasswordInput.setEnabled(enabled);
        confirmPasswordInput.setEnabled(enabled);
        changePasswordButton.setEnabled(enabled);

        // Hiển thị loading text trên button
        if (enabled) {
            changePasswordButton.setText("Change Password");
        } else {
            changePasswordButton.setText("Đang xử lý...");
        }
    }

    /**
     * Chuyển về màn hình login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(change_password.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}