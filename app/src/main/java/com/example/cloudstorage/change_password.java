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

import org.json.JSONObject;

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

        tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

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

    private void handleChangePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            currentPasswordInput.setError("Please enter current password");
            currentPasswordInput.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordInput.setError("Please enter new password");
            newPasswordInput.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            newPasswordInput.setError("New password must be at least 8 characters");
            newPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            newPasswordInput.setError("Password must contain at least 1 letter, 1 number and 1 special character");
            newPasswordInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm password");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            newPasswordInput.setError("New password must be different from current password");
            newPasswordInput.requestFocus();
            return;
        }

        setInputsEnabled(false);

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        ApiClient.getApiService(this).changePassword(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        setInputsEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            Toast.makeText(change_password.this,
                                    apiResponse.getMessageOrDefault("Password changed successfully!"),
                                    Toast.LENGTH_SHORT).show();

                            currentPasswordInput.setText("");
                            newPasswordInput.setText("");
                            confirmPasswordInput.setText("");

                            finish();

                        } else {
                            String errorMsg = "Failed to change password";

                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JSONObject obj = new JSONObject(errorJson);
                                    errorMsg = obj.optString("message", "Failed to change password");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(change_password.this, errorMsg, Toast.LENGTH_LONG).show();

                            if (response.code() == 401) {
                                tokenManager.clearToken();
                                navigateToLogin();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        setInputsEnabled(true);

                        String errorMsg = "Connection error: " + t.getMessage();
                        Toast.makeText(change_password.this, errorMsg, Toast.LENGTH_LONG).show();

                        t.printStackTrace();
                    }
                });
    }

    private void setInputsEnabled(boolean enabled) {
        currentPasswordInput.setEnabled(enabled);
        newPasswordInput.setEnabled(enabled);
        confirmPasswordInput.setEnabled(enabled);
        changePasswordButton.setEnabled(enabled);

        if (enabled) {
            changePasswordButton.setText("Change Password");
        } else {
            changePasswordButton.setText("Processing...");
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(change_password.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}