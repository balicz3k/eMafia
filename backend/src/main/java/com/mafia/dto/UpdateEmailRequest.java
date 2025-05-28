package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update user's email address")
public class UpdateEmailRequest {

    @Schema(description = "New email address", example = "newemail@example.com", format = "email", required = true)
    @NotBlank(message = "New email is required")
    @Email(message = "Email format is invalid")
    private String newEmail;

    public UpdateEmailRequest() {
    }

    public UpdateEmailRequest(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}