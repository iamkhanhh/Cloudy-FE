package com.example.cloudstorage.models;

/**
 * Request body cho POST /auth/activate-account/:id
 * Body: { "code": "123456" }
 */
public class VerifyAccountRequest {
    private String code;

    public VerifyAccountRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
