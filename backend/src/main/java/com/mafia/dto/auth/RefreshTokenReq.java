package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Request to refresh access token")
public class RefreshTokenReq {

  @Schema(
      description = "Refresh token",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "refresh_abc123...")
  @NotBlank(message = "Refresh token is required")
  private String refreshToken;

  public RefreshTokenReq() {}

  public RefreshTokenReq(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
