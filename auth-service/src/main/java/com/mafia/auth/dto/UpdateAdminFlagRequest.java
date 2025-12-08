package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request do aktualizacji flagi admin użytkownika.
 */
@Schema(description = "Request to update user's admin flag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminFlagRequest {

    @Schema(
        description = "Whether the user should be an admin",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean admin;
}
