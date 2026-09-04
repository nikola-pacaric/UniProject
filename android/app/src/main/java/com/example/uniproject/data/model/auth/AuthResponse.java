package com.example.uniproject.data.model.auth;

public class AuthResponse {
    private String token;
    private String username;
    private String fullName;
    private String message;

    public AuthResponse() {
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMessage() {
        return message;
    }
}
