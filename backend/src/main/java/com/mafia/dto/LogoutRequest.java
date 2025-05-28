package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to logout user")
public class LogoutRequest {

    @Schema(description = "Refresh token to revoke", example = "refresh_abc123...")
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}