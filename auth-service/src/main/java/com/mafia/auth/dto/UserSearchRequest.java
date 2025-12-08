package com.mafia.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request do wyszukiwania użytkowników.
 */
@Schema(description = "Request to search for users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {

    @Schema(
        description = "Search query (username or email)",
        example = "john",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String query;
}
