package com.example.cloudstorage;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VerificationPage extends AppCompatActivity {

    private EditText codeEditText;
    private TextView errorTextView;
    private Button verifyButton, resendButton;
    private TextView tvDidntReceive;

    // Simulate correct OTP for testing
    private final String correctOTP = "123456";

    private boolean canResend = false;
    private final int resendDelay = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification_page);

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
        verifyButton.setOnClickListener(v -> {
            String enteredOTP = codeEditText.getText().toString().trim();
            if (enteredOTP.equals(correctOTP)) {
                Toast.makeText(VerificationPage.this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show();
                errorTextView.setVisibility(View.GONE);
            } else {
                errorTextView.setVisibility(View.VISIBLE);
            }
        });

        // Resend OTP button (disabled initially)
        resendButton.setEnabled(false);
        startResendTimer();

        resendButton.setOnClickListener(v -> {
            if (canResend) {
                Toast.makeText(VerificationPage.this, "New OTP sent!", Toast.LENGTH_SHORT).show();
                canResend = false;
                resendButton.setEnabled(false);
                startResendTimer();
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
}