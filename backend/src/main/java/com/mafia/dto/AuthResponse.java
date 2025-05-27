package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token")
public class AuthResponse {

    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjIzNzU4NDAwLCJleHAiOjE2MjM4NDQ4MDB9.signature", required = true)
    private String token;

    @Schema(description = "Token type (always 'Bearer' for JWT)", example = "Bearer", required = true, defaultValue = "Bearer")
    private String tokenType = "Bearer";

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}