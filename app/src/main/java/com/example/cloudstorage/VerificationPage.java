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

    private int userId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification_page);

        userId = getIntent().getIntExtra("userId", -1);
        userEmail = getIntent().getStringExtra("email");

        if (userId == -1 || userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Invalid information. Please register again.", Toast.LENGTH_LONG).show();
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

    private void handleVerifyOtp() {
        String code = codeEditText.getText().toString().trim();

        if (code.isEmpty()) {
            errorTextView.setText("Please enter verification code");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        if (code.length() < 6) {
            errorTextView.setText("Verification code format is invalid");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        showVerifyLoading(true);

        VerifyAccountRequest request = new VerifyAccountRequest(code);

        ApiClient.getApiService(this).activateAccount(userId, request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        showVerifyLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            Toast.makeText(VerificationPage.this,
                                    apiResponse.getMessageOrDefault("Verification successful!"),
                                    Toast.LENGTH_SHORT).show();

                            navigateToLogin();

                        } else {
                            String errorMsg = "Verification code is incorrect or expired";

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

                        String errorMsg = "Connection error: " + t.getMessage();
                        errorTextView.setText(errorMsg);
                        errorTextView.setVisibility(View.VISIBLE);

                        t.printStackTrace();
                    }
                });
    }

    private void handleResendOtp() {
        showResendLoading(true);

        ResendOtpRequest request = new ResendOtpRequest(userEmail);

        ApiClient.getApiService(this).resendOtp(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        showResendLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            Toast.makeText(VerificationPage.this,
                                    apiResponse.getMessageOrDefault("New OTP has been sent!"),
                                    Toast.LENGTH_SHORT).show();

                            canResend = false;
                            resendButton.setEnabled(false);
                            startResendTimer();

                        } else {
                            String errorMsg = "Cannot resend OTP";

                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(VerificationPage.this, errorMsg, Toast.LENGTH_LONG).show();

                            canResend = true;
                            resendButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        showResendLoading(false);

                        String errorMsg = "Connection error: " + t.getMessage();
                        Toast.makeText(VerificationPage.this, errorMsg, Toast.LENGTH_LONG).show();

                        canResend = true;
                        resendButton.setEnabled(true);

                        t.printStackTrace();
                    }
                });
    }

    private void showVerifyLoading(boolean isLoading) {
        if (isLoading) {
            verifyButton.setEnabled(false);
            verifyButton.setText("Verifying...");
        } else {
            verifyButton.setEnabled(true);
            verifyButton.setText("Continue");
        }
    }

    private void showResendLoading(boolean isLoading) {
        if (isLoading) {
            resendButton.setEnabled(false);
            resendButton.setText("Sending...");
        } else {
            resendButton.setText("Resend");
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(VerificationPage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}