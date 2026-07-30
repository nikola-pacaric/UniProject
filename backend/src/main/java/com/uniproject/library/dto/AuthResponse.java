package com.uniproject.library.dto;

public class AuthResponse {

    private String token;
    private String username;
    private String fullName;
    private String message;

    public AuthResponse() {super();}

    public AuthResponse(String token, String username, String fullName, String message) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
    
}
