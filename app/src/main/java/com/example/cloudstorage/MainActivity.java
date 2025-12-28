package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
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
        setContentView(R.layout.component_welcome_page);

        tokenManager = new TokenManager(this);

        if (tokenManager.isLoggedIn()) {
            navigateToHomePage();
            return;
        }

        if (getIntent().getBooleanExtra("session_expired", false)) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
        }

        signInButton = findViewById(R.id.btn_submit);
        createAccountLink = findViewById(R.id.createAccountLink);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });


        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email cannot be empty");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password cannot be empty");
            passwordInput.requestFocus();
            return;
        }

        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(email, password);

        ApiClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String accessToken = loginResponse.getAccessToken();

                    tokenManager.saveToken(accessToken);
                    tokenManager.saveUserEmail(email);

                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    navigateToHomePage();

                } else {
                    String errorMsg = "Login failed. Please check your email and password.";

                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JSONObject obj = new JSONObject(errorJson);
                            errorMsg = obj.optString("message", "Login failed. Please check your email and password.");
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

                String errorMsg = "Connection error: " + t.getMessage();
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();

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