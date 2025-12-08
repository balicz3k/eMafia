package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request do aktualizacji hasła użytkownika.
 */
@Schema(description = "Request to update user's password")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    @Schema(
        description = "Current password for verification",
        example = "OldSecurePassword123!",
        format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @Schema(
        description = "New password (minimum 8 characters)",
        example = "NewSecurePassword456!",
        format = "password",
        minLength = 8,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
