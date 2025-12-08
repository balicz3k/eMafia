package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response zawierający informacje o użytkowniku.
 */
@Schema(description = "User information response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(
        description = "User's unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(description = "User's username", example = "john_doe")
    private String username;

    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "Is user an admin of the service", example = "false")
    private boolean isAdmin;
}
