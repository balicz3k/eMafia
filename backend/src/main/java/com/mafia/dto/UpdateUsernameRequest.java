package com.mafia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUsernameRequest
{
    @NotBlank(message = "New username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String newUsername;

    public String getNewUsername() { return newUsername; }

    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
}