package com.mafia.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to update user's username")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameReq {

  @Schema(
      description = "New username (3-20 characters, alphanumeric and underscore only)",
      example = "new_cool_username",
      minLength = 3,
      maxLength = 20,
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "New username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String newUsername;
}
