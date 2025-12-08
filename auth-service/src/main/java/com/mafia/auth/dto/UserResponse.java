package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserResponse {
  
  @Schema(description = "User ID")
  private UUID id;
  
  @Schema(description = "Username", example = "john_doe")
  private String username;
  
  @Schema(description = "Email address", example = "john@example.com")
  private String email;
  
  @Schema(description = "Whether user is admin")
  private boolean isAdmin;
  
  @Schema(description = "Account creation date")
  private LocalDateTime createdAt;
}
