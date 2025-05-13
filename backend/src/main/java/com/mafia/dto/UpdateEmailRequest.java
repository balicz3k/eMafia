package com.mafia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateEmailRequest
{
    @NotBlank(message = "New email is required") @Email(message = "Invalid email format") private String newEmail;

    public String getNewEmail() { return newEmail; }

    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}