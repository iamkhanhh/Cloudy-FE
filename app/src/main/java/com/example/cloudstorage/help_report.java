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
import com.example.cloudstorage.models.ReportRequest;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class help_report extends AppCompatActivity {

    private TextInputEditText titleInput;
    private TextInputEditText detailsInput;
    private Button submitButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_report);

        tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        titleInput = findViewById(R.id.tile);
        detailsInput = findViewById(R.id.details);
        submitButton = findViewById(R.id.btn_submit);
        ImageView btn_back = findViewById(R.id.btn_back);

        // Back button
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitReport();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void handleSubmitReport() {
        String title = titleInput.getText().toString().trim();
        String details = detailsInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Please enter title");
            titleInput.requestFocus();
            return;
        }

        if (details.isEmpty()) {
            detailsInput.setError("Please describe the issue");
            detailsInput.requestFocus();
            return;
        }

        if (details.length() < 10) {
            detailsInput.setError("Please provide more details (minimum 10 characters)");
            detailsInput.requestFocus();
            return;
        }

        setInputsEnabled(false);

        ReportRequest request = new ReportRequest(title, details);

        ApiClient.getApiService(this).submitReport(request)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        setInputsEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();

                            Toast.makeText(help_report.this,
                                    apiResponse.getMessageOrDefault("Report submitted successfully!"),
                                    Toast.LENGTH_LONG).show();

                            titleInput.setText("");
                            detailsInput.setText("");

                            finish();

                        } else {
                            String errorMsg = "Failed to submit report";

                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    JSONObject obj = new JSONObject(errorJson);
                                    errorMsg = obj.optString("message", "Failed to submit report");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(help_report.this, errorMsg, Toast.LENGTH_LONG).show();

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
                        Toast.makeText(help_report.this, errorMsg, Toast.LENGTH_LONG).show();

                        t.printStackTrace();
                    }
                });
    }

    private void setInputsEnabled(boolean enabled) {
        titleInput.setEnabled(enabled);
        detailsInput.setEnabled(enabled);
        submitButton.setEnabled(enabled);

        if (enabled) {
            submitButton.setText("Submit");
        } else {
            submitButton.setText("Submitting...");
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(help_report.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}