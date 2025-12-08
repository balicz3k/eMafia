package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
  @Schema(description = "Refresh token", example = "refresh_abc123...")
  @NotBlank(message = "Refresh token is required")
  private String refreshToken;
}
