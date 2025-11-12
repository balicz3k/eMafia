package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User information response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResp {
  @Schema(
      description = "User's unique identifier",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "User's username", example = "john_doe")
  private String username;

  @Schema(description = "User's email address", example = "john@example.com")
  private String email;

  @Schema(description = "Is user an admin of the service", example = "false")
  private boolean isAdmin;
}
