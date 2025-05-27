package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.mafia.models.Role;
import java.util.Set;
import java.util.UUID;

@Schema(description = "User information response")
public class UserResponse {
    @Schema(description = "User's unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User's username", example = "john_doe")
    private String username;

    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "User's roles in the system", example = "[\"ROLE_USER\"]")
    private Set<Role> roles;

    public UserResponse(UUID id, String username, String email, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
