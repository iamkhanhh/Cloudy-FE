package com.example.cloudstorage.models;

/**
 * Request body cho POST /auth/resend-otp
 * Body: { "email": "user@example.com" }
 */
public class ResendOtpRequest {
    private String email;

    public ResendOtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
