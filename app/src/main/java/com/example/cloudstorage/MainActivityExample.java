//package com.example.cloudstorage;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.cloudstorage.api.ApiClient;
//import com.example.cloudstorage.models.LoginRequest;
//import com.example.cloudstorage.models.LoginResponse;
//import com.example.cloudstorage.utils.TokenManager;
//import com.google.android.material.textfield.TextInputEditText;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
///**
// * MainActivity với Retrofit Integration
// * File này là VÍ DỤ - Bạn có thể copy logic này vào MainActivity.java của bạn
// */
//public class MainActivityExample extends AppCompatActivity {
//
//    private TextInputEditText emailInput;
//    private TextInputEditText passwordInput;
//    private Button signInButton;
//    private TextView createAccountLink;
//    private ProgressBar progressBar;
//    private TextView errorMessage;
//
//    private TokenManager tokenManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.welcome_page);
//
//        // Khởi tạo TokenManager
//        tokenManager = new TokenManager(this);
//
//        // Kiểm tra xem user đã login chưa
//        // Nếu đã login (có token), chuyển thẳng đến HomePage
//        if (tokenManager.isLoggedIn()) {
//            navigateToHomePage();
//            return;
//        }
//
//        // Kiểm tra nếu session expired (từ AuthInterceptor)
//        if (getIntent().getBooleanExtra("session_expired", false)) {
//            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
//        }
//
//        // Tìm các View từ XML
//        emailInput = findViewById(R.id.emailInput); // Thay R.id.emailInput bằng ID thực của bạn
//        passwordInput = findViewById(R.id.passwordInput); // Thay R.id.passwordInput bằng ID thực
//        signInButton = findViewById(R.id.button2);
//        createAccountLink = findViewById(R.id.createAccountLink);
//        // progressBar = findViewById(R.id.progressBar); // Nếu có
//        // errorMessage = findViewById(R.id.errorMessage); // Nếu có
//
//        // Xử lý nút Sign In
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handleLogin();
//            }
//        });
//
//        // Xử lý link Create Account
//        createAccountLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivityExample.this, CreateAccountActivity.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    /**
//     * Xử lý đăng nhập
//     */
//    private void handleLogin() {
//        // Lấy email và password từ input
//        String email = emailInput.getText().toString().trim();
//        String password = passwordInput.getText().toString().trim();
//
//        // Validate input
//        if (email.isEmpty()) {
//            emailInput.setError("Email không được để trống");
//            emailInput.requestFocus();
//            return;
//        }
//
//        if (password.isEmpty()) {
//            passwordInput.setError("Password không được để trống");
//            passwordInput.requestFocus();
//            return;
//        }
//
//        // Hiển thị loading
//        showLoading(true);
//
//        // Tạo LoginRequest
//        LoginRequest loginRequest = new LoginRequest(email, password);
//
//        // Gọi API login
//        ApiClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                showLoading(false);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    // Login thành công
//                    LoginResponse loginResponse = response.body();
//                    String accessToken = loginResponse.getAccessToken();
//
//                    // Lưu token vào SharedPreferences
//                    tokenManager.saveToken(accessToken);
//                    tokenManager.saveUserEmail(email);
//
//                    Toast.makeText(MainActivityExample.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//
//                    // Chuyển đến HomePage
//                    navigateToHomePage();
//
//                } else {
//                    // Login thất bại
//                    String errorMsg = "Đăng nhập thất bại. Vui lòng kiểm tra lại email và password.";
//
//                    // Parse error message từ backend nếu có
//                    try {
//                        if (response.errorBody() != null) {
//                            errorMsg = response.errorBody().string();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    Toast.makeText(MainActivityExample.this, errorMsg, Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                showLoading(false);
//
//                // Lỗi network hoặc lỗi khác
//                String errorMsg = "Lỗi kết nối: " + t.getMessage();
//                Toast.makeText(MainActivityExample.this, errorMsg, Toast.LENGTH_LONG).show();
//
//                // Log lỗi để debug
//                t.printStackTrace();
//            }
//        });
//    }
//
//    /**
//     * Chuyển đến HomePage và clear back stack
//     */
//    private void navigateToHomePage() {
//        Intent intent = new Intent(MainActivityExample.this, HomePage.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }
//
//    /**
//     * Hiển thị/ẩn loading indicator
//     */
//    private void showLoading(boolean isLoading) {
//        if (isLoading) {
//            signInButton.setEnabled(false);
//            signInButton.setText("Đang đăng nhập...");
//            // if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
//        } else {
//            signInButton.setEnabled(true);
//            signInButton.setText("Sign In");
//            // if (progressBar != null) progressBar.setVisibility(View.GONE);
//        }
//    }
//}
