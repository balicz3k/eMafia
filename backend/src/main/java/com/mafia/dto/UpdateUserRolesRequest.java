package com.mafia.dto;

import com.mafia.models.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UpdateUserRolesRequest
{
    @NotEmpty(message = "Roles cannot be empty") private Set<Role> roles;

    public Set<Role> getRoles() { return roles; }

    public void setRoles(Set<Role> roles) { this.roles = roles; }
}