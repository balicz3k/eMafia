package com.mafia.dto;
import com.mafia.models.Role;
import java.util.Set;
import java.util.UUID;
public class UserResponse
{
    private UUID id;
    private String username;
    private String email;
    private Set<Role> roles;

    public UserResponse(UUID id, String username, String email, Set<Role> roles)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    // Gettery i settery
    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Set<Role> getRoles() { return roles; }

    public void setRoles(Set<Role> roles) { this.roles = roles; } // Ustawiamy rolę użytkownika
}
