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
        setContentView(R.layout.activity_create_account);

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

    private void handleRegister() {
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

        setInputsEnabled(false);

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

        ApiClient.getApiService(this).register(request)
                .enqueue(new Callback<ApiResponse<RegisterResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<RegisterResponse>> call, @NonNull Response<ApiResponse<RegisterResponse>> response) {
                        setInputsEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<RegisterResponse> apiResponse = response.body();

                            if (apiResponse.getData() != null) {
                                RegisterResponse registerResponse = apiResponse.getData();

                                Toast.makeText(CreateAccountActivity.this,
                                        apiResponse.getMessageOrDefault("Registration successful! Please check your email to verify your account."),
                                        Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(CreateAccountActivity.this, VerificationPage.class);
                                intent.putExtra("userId", registerResponse.getId());
                                intent.putExtra("email", registerResponse.getEmail());
                                startActivity(intent);

                                finish();
                            } else {
                                Toast.makeText(CreateAccountActivity.this, "No response from server", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            String errorMsg = "Registration failed";

                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JSONObject obj = new JSONObject(errorJson);
                                    errorMsg = obj.optString("message", "Registration failed");
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

                        String errorMsg = "Connection error: " + t.getMessage();
                        Toast.makeText(CreateAccountActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                        t.printStackTrace();
                    }
                });
    }

    private boolean validateInputs(String firstName, String lastName, String email, String password, String confirmPassword) {
        if (firstName.isEmpty()) {
            firstNameInput.setError("Please enter first name");
            firstNameInput.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            lastNameInput.setError("Please enter last name");
            lastNameInput.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError("Please enter email");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Please enter password");
            passwordInput.requestFocus();
            return false;
        }

        if (!isValidPassword(password)) {
            passwordInput.setError("Password must be at least 8 characters with letters, numbers and special characters (@$!%*#?&)");
            passwordInput.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm password");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        return password.matches(pattern);
    }

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

        if (enabled) {
            signUpButton.setText("Sign Up");
        } else {
            signUpButton.setText("Creating account...");
        }
    }
}
