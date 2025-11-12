package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {
  @Schema(
      description = "User's email address used for authentication",
      example = "john@example.com",
      format = "email",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @Schema(
      description = "User's password for authentication",
      example = "SecurePassword123!",
      minLength = 1,
      requiredMode = Schema.RequiredMode.REQUIRED,
      format = "password")
  @NotBlank(message = "Password is required")
  private String password;
}
