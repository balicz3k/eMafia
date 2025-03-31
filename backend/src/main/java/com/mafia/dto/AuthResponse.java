package com.mafia.dto;

public class AuthResponse
{
    private String token;
    private String tokenType = "Bearer";

    public AuthResponse(String token) { this.token = token; }

    // Gettery

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
}