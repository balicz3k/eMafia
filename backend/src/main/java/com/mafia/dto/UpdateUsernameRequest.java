package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update user's username")
public class UpdateUsernameRequest {

    @Schema(description = "New username (3-20 characters, alphanumeric and underscore only)", example = "new_cool_username", minLength = 3, maxLength = 20, required = true)
    @NotBlank(message = "New username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String newUsername;

    public UpdateUsernameRequest() {
    }

    public UpdateUsernameRequest(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}