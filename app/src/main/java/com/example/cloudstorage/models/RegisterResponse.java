package com.example.cloudstorage.models;

/**
 * Response model for registration
 * Contains user ID and email needed for account verification
 */
public class RegisterResponse {
    private int id;
    private String email;

    public RegisterResponse() {}

    public RegisterResponse(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
