package com.example.cloudstorage.models;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API Response from Backend
 * Supports multiple response patterns:
 * 1. Simple: { "status": "success", "message": "..." }
 * 2. With data: { "status": "success", "message": "...", "data": {...} }
 * 3. Data only: { "status": "success", "data": {...} }
 *
 * @param <T> The type of data returned (use Void for responses without data)
 */
public class ApiResponse<T> {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // Utility methods
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public boolean hasData() {
        return data != null;
    }

    public String getMessageOrDefault(String defaultMessage) {
        return message != null ? message : defaultMessage;
    }
}
