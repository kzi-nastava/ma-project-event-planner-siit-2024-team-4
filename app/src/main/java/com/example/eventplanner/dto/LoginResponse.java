package com.example.eventplanner.dto;

public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String role;

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
