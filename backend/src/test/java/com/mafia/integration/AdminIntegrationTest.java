package com.mafia.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.*;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestApplicationConfig.class)
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register regular user
        RegistrationRequest userRequest = new RegistrationRequest();
        userRequest.setUsername("regularUser");
        userRequest.setEmail("regular@example.com");
        userRequest.setPassword("Password123!");

        MvcResult userResult = mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse userAuth = objectMapper.readValue(
                userResult.getResponse().getContentAsString(), AuthResponse.class);
        this.regularUserToken = userAuth.getToken();

        // Register a user who will become admin
        RegistrationRequest adminCandidateRequest = new RegistrationRequest();
        adminCandidateRequest.setUsername("adminUser");
        adminCandidateRequest.setEmail("admin@example.com");
        adminCandidateRequest.setPassword("AdminPass123!");

        // 1. Register the user (they will initially have ROLE_USER)
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminCandidateRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // 2. Manually promote the user to admin in the database
        User adminUser = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new IllegalStateException("Admin candidate user not found after registration"));
        adminUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))); // Assign admin role (and user role
                                                                                    // if needed)
        userRepository.save(adminUser);

        // 3. Log in as the now-admin user to get a token with admin privileges
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setPassword("AdminPass123!");

        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse adminAuthResponse = objectMapper.readValue(
                adminLoginResult.getResponse().getContentAsString(), AuthResponse.class);
        this.adminToken = adminAuthResponse.getToken(); // This token will have admin roles
    }

    @Test
    void adminFlow_manageUsers_requiresAdminRole() throws Exception {
        // Regular user trying to access admin endpoints should fail
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());

        // Admin user should succeed
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminFlow_getAllUsers_success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void adminFlow_updateUserRoles_success() throws Exception {
        User regularUser = userRepository.findByEmail("regular@example.com").orElseThrow();

        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of(Role.ROLE_ADMIN));

        mockMvc.perform(put("/api/admin/users/{userId}/roles", regularUser.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.containsInAnyOrder("ROLE_ADMIN")));
    }
}