package com.mafia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import com.mafia.models.Role;

@Schema(description = "Request to update user roles")
public class UpdateUserRolesRequest {

    @Schema(description = "Set of roles to assign to the user", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]", allowableValues = {
            "ROLE_USER", "ROLE_ADMIN" }, required = true)
    @NotNull(message = "Roles set is required")
    @NotEmpty(message = "At least one role must be specified")
    private Set<Role> roles;

    public UpdateUserRolesRequest() {
    }

    public UpdateUserRolesRequest(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}