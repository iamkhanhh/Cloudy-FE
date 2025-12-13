package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.RegisterRequest;
import com.example.cloudstorage.models.RegisterResponse;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneNumberInput;
    private TextInputEditText addressInput;
    private TextInputEditText bioInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_account);

        // Khởi tạo views
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        addressInput = findViewById(R.id.address_input);
        bioInput = findViewById(R.id.bio_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signUpButton = findViewById(R.id.btn_signup);

        View backToLogin = findViewById(R.id.back_to_login);

        // Back to login button
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(CreateAccountActivity.this, MainActivity.class);
                startActivity(it);
            }
        });

        // Sign up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Xử lý đăng ký tài khoản
     */
    private void handleRegister() {
        // Lấy giá trị từ input fields
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(firstName, lastName, email, password, confirmPassword)) {
            return;
        }

        // Disable button và hiển thị loading
        setInputsEnabled(false);

        // Tạo request
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);

        // Set optional fields if not empty
        if (!phoneNumber.isEmpty()) {
            request.setPhoneNumber(phoneNumber);
        }
        if (!address.isEmpty()) {
            request.setAddress(address);
        }
        if (!bio.isEmpty()) {
            request.setBio(bio);
        }

        // Gọi API register
        ApiClient.getApiService(this).register(request)
                .enqueue(new Callback<ApiResponse<RegisterResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<RegisterResponse>> call, @NonNull Response<ApiResponse<RegisterResponse>> response) {
                        setInputsEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<RegisterResponse> apiResponse = response.body();

                            if (apiResponse.getData() != null) {
                                RegisterResponse registerResponse = apiResponse.getData();

                                // Hiển thị thông báo thành công
                                Toast.makeText(CreateAccountActivity.this,
                                        apiResponse.getMessageOrDefault("Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản."),
                                        Toast.LENGTH_LONG).show();

                                // Chuyển đến trang xác thực với userId và email
                                Intent intent = new Intent(CreateAccountActivity.this, VerificationPage.class);
                                intent.putExtra("userId", registerResponse.getId());
                                intent.putExtra("email", registerResponse.getEmail());
                                startActivity(intent);

                                // Đóng activity hiện tại
                                finish();
                            } else {
                                Toast.makeText(CreateAccountActivity.this, "Không nhận được thông tin từ server", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // Xử lý lỗi
                            String errorMsg = "Đăng ký thất bại";

                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JSONObject obj = new JSONObject(errorJson);
                                    errorMsg = obj.optString("message", "Đăng ký thất bại");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(CreateAccountActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<RegisterResponse>> call, @NonNull Throwable t) {
                        setInputsEnabled(true);

                        String errorMsg = "Lỗi kết nối: " + t.getMessage();
                        Toast.makeText(CreateAccountActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                        t.printStackTrace();
                    }
                });
    }

    /**
     * Validate tất cả input fields
     */
    private boolean validateInputs(String firstName, String lastName, String email, String password, String confirmPassword) {
        // Validate first name
        if (firstName.isEmpty()) {
            firstNameInput.setError("Vui lòng nhập tên");
            firstNameInput.requestFocus();
            return false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            lastNameInput.setError("Vui lòng nhập họ");
            lastNameInput.requestFocus();
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            emailInput.setError("Vui lòng nhập email");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email không hợp lệ");
            emailInput.requestFocus();
            return false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            passwordInput.requestFocus();
            return false;
        }

        if (!isValidPassword(password)) {
            passwordInput.setError("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ cái, số và ký tự đặc biệt (@$!%*#?&)");
            passwordInput.requestFocus();
            return false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Vui lòng xác nhận mật khẩu");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mật khẩu xác nhận không khớp");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra mật khẩu có đáp ứng yêu cầu không
     * Ít nhất 8 ký tự, có chữ cái, số và ký tự đặc biệt
     */
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        return password.matches(pattern);
    }

    /**
     * Enable/disable inputs và button
     */
    private void setInputsEnabled(boolean enabled) {
        firstNameInput.setEnabled(enabled);
        lastNameInput.setEnabled(enabled);
        emailInput.setEnabled(enabled);
        phoneNumberInput.setEnabled(enabled);
        addressInput.setEnabled(enabled);
        bioInput.setEnabled(enabled);
        passwordInput.setEnabled(enabled);
        confirmPasswordInput.setEnabled(enabled);
        signUpButton.setEnabled(enabled);

        // Hiển thị loading text trên button
        if (enabled) {
            signUpButton.setText("Sign Up");
        } else {
            signUpButton.setText("Đang tạo tài khoản...");
        }
    }
}
