package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.LoginRequest;
import com.example.cloudstorage.models.LoginResponse;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextInputEditText emailInput;
    TextInputEditText passwordInput;
    Button signInButton;
    View createAccountLink;
    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_page);

        tokenManager = new TokenManager(this);

        if (tokenManager.isLoggedIn()) {
            navigateToHomePage();
            return;
        }

        if (getIntent().getBooleanExtra("session_expired", false)) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        }

        // 1. Tìm các View bằng ID của chúng từ file XML
        signInButton = findViewById(R.id.btn_submit);
        createAccountLink = findViewById(R.id.createAccountLink);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);

        // 2. Đặt OnClickListener cho nút "Sign In"
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });


        // 3. Đặt OnClickListener cho văn bản "Create an account"
        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo một Intent để mở CreateAccountActivity
                Intent createAccountIntent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(createAccountIntent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void handleLogin() {
        // Lấy email và password từ input
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (email.isEmpty()) {
            emailInput.setError("Email không được để trống");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password không được để trống");
            passwordInput.requestFocus();
            return;
        }

        // Hiển thị loading
        showLoading(true);

        // Tạo LoginRequest
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Gọi API login
        ApiClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Login thành công
                    LoginResponse loginResponse = response.body();
                    String accessToken = loginResponse.getAccessToken();

                    // Lưu token vào SharedPreferences
                    tokenManager.saveToken(accessToken);
                    tokenManager.saveUserEmail(email);

                    Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển đến HomePage
                    navigateToHomePage();

                } else {
                    // Login thất bại
                    String errorMsg = "Đăng nhập thất bại. Vui lòng kiểm tra lại email và password.";

                    // Parse error message từ backend nếu có
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JSONObject obj = new JSONObject(errorJson);
                            errorMsg = obj.optString("message", "Đăng nhập thất bại. Vui lòng kiểm tra lại email và password.");
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                showLoading(false);

                // Lỗi network hoặc lỗi khác
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                // Log lỗi để debug
                t.printStackTrace();
            }
        });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(MainActivity.this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            signInButton.setEnabled(false);
            signInButton.setText("Loading...");
        } else {
            signInButton.setEnabled(true);
            signInButton.setText("Sign In");
        }
    }
}