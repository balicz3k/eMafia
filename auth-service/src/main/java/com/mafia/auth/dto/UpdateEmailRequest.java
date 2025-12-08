package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request do aktualizacji adresu email użytkownika.
 */
@Schema(description = "Request to update user's email address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailRequest {

    @Schema(
        description = "New email address",
        example = "newemail@example.com",
        format = "email",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "New email is required")
    @Email(message = "Email format is invalid")
    private String newEmail;
}
