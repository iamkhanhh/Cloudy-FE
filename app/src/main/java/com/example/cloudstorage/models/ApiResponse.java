package com.example.cloudstorage.models;

/**
 * Generic API Response từ Backend
 * Dùng cho các endpoint trả về: { "status": "success", "message": "..." }
 */
public class ApiResponse {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
