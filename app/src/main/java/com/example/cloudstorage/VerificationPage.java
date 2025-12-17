package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.cloudstorage.models.ResendOtpRequest;
import com.example.cloudstorage.models.VerifyAccountRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationPage extends AppCompatActivity {

    private EditText codeEditText;
    private TextView errorTextView;
    private Button verifyButton, resendButton;
    private TextView tvDidntReceive;

    private boolean canResend = false;
    private final int resendDelay = 30000;

    // Nhận userId và email từ Intent
    private int userId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification_page);

        // Nhận userId và email từ Intent (từ màn hình Register)
        userId = getIntent().getIntExtra("userId", -1);
        userEmail = getIntent().getStringExtra("email");

        if (userId == -1 || userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Thông tin không hợp lệ. Vui lòng đăng ký lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        codeEditText = findViewById(R.id.code);
        errorTextView = findViewById(R.id.tvError);
        verifyButton = findViewById(R.id.btnContinue);
        resendButton = findViewById(R.id.btnResend);
        tvDidntReceive = findViewById(R.id.tvDidntReceive);

        // Restrict input to numbers only
        codeEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        // Enable verify button when something is typed
        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                verifyButton.setEnabled(s.length() > 0);
                errorTextView.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Verify OTP
        verifyButton.setOnClickListener(v -> handleVerifyOtp());

        // Resend OTP button (disabled initially)
        resendButton.setEnabled(false);
        startResendTimer();

        resendButton.setOnClickListener(v -> {
            if (canResend) {
                handleResendOtp();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startResendTimer() {
        new Handler().postDelayed(() -> {
            canResend = true;
            resendButton.setEnabled(true);
        }, resendDelay);
    }

    /**
     * Xử lý xác thực OTP
     */
    private void handleVerifyOtp() {
        String code = codeEditText.getText().toString().trim();

        // Validate input
        if (code.isEmpty()) {
            errorTextView.setText("Vui lòng nhập mã xác thực");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        if (code.length() < 6) {
            errorTextView.setText("Mã xác thực không đúng định dạng");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        // Hiển thị loading
        showVerifyLoading(true);

        // Tạo request
        VerifyAccountRequest request = new VerifyAccountRequest(code);

        // Gọi API
        ApiClient.getApiService(this).activateAccount(userId, request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        showVerifyLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            // Xác thực thành công
                            Toast.makeText(VerificationPage.this,
                                    apiResponse.getMessageOrDefault("Xác thực thành công!"),
                                    Toast.LENGTH_SHORT).show();

                            // Chuyển về màn hình login
                            navigateToLogin();

                        } else {
                            // Xác thực thất bại
                            String errorMsg = "Mã xác thực không đúng hoặc đã hết hạn";

                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            errorTextView.setText(errorMsg);
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        showVerifyLoading(false);

                        String errorMsg = "Lỗi kết nối: " + t.getMessage();
                        errorTextView.setText(errorMsg);
                        errorTextView.setVisibility(View.VISIBLE);

                        t.printStackTrace();
                    }
                });
    }

    /**
     * Xử lý gửi lại OTP
     */
    private void handleResendOtp() {
        // Hiển thị loading
        showResendLoading(true);

        // Tạo request
        ResendOtpRequest request = new ResendOtpRequest(userEmail);

        // Gọi API
        ApiClient.getApiService(this).resendOtp(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        showResendLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            Toast.makeText(VerificationPage.this,
                                    apiResponse.getMessageOrDefault("Mã OTP mới đã được gửi!"),
                                    Toast.LENGTH_SHORT).show();

                            // Reset timer
                            canResend = false;
                            resendButton.setEnabled(false);
                            startResendTimer();

                        } else {
                            String errorMsg = "Không thể gửi lại mã OTP";

                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(VerificationPage.this, errorMsg, Toast.LENGTH_LONG).show();

                            // Vẫn cho phép resend lại nếu thất bại
                            canResend = true;
                            resendButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        showResendLoading(false);

                        String errorMsg = "Lỗi kết nối: " + t.getMessage();
                        Toast.makeText(VerificationPage.this, errorMsg, Toast.LENGTH_LONG).show();

                        // Vẫn cho phép resend lại nếu thất bại
                        canResend = true;
                        resendButton.setEnabled(true);

                        t.printStackTrace();
                    }
                });
    }

    /**
     * Hiển thị/ẩn loading cho verify button
     */
    private void showVerifyLoading(boolean isLoading) {
        if (isLoading) {
            verifyButton.setEnabled(false);
            verifyButton.setText("Đang xác thực...");
        } else {
            verifyButton.setEnabled(true);
            verifyButton.setText("Continue");
        }
    }

    /**
     * Hiển thị/ẩn loading cho resend button
     */
    private void showResendLoading(boolean isLoading) {
        if (isLoading) {
            resendButton.setEnabled(false);
            resendButton.setText("Đang gửi...");
        } else {
            resendButton.setText("Resend");
        }
    }

    /**
     * Chuyển về màn hình login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(VerificationPage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}