package com.example.myprojectmvvmrestapi.model;

public class AuthResponse {
    private String name, email, token, token_type;

    public AuthResponse(String name, String email, String token, String token_type) {
        this.name = name;
        this.email = email;
        this.token = token;
        this.token_type = token_type;
    }

    public String getName() {
        return name;
    }
    public String getToken() {
        return token;
    }
    public String getToken_type() {
        return token_type;
    }
}
