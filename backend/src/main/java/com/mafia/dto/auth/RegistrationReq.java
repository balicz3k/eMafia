package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegistrationReq {
  @Schema(
      description = "Username (3-20 characters, alphanumeric and underscore only)",
      example = "john_doe",
      minLength = 3,
      maxLength = 20)
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String username;

  @Schema(description = "Valid email address", example = "john@example.com", format = "email")
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @Schema(
      description = "Password (minimum 8 characters)",
      example = "SecurePassword123!",
      minLength = 8)
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
      message =
          "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
  private String password;
}
