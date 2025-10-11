package com.example.eventplanner.network.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String role;

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
