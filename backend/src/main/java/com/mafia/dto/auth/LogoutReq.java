package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to logout user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutReq {

  @Schema(description = "Refresh token to revoke", example = "refresh_abc123...")
  private String refreshToken;
}
