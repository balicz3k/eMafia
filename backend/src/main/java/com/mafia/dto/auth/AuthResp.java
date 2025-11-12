package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Authentication response containing access and refresh tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResp {

  @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
  private String token;

  @Schema(
      description = "Refresh token for obtaining new access tokens",
      example = "refresh_abc123...")
  private String refreshToken;

  @Schema(description = "Token type", example = "Bearer")
  private String tokenType = "Bearer";

  @Schema(description = "Access token expiration time in seconds", example = "3600")
  private long expiresIn;
}
