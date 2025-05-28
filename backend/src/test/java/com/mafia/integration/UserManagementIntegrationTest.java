package com.mafia.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.*;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestApplicationConfig.class)
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        RegistrationRequest registerRequest = new RegistrationRequest();
        registerRequest.setUsername("profileUser");
        registerRequest.setEmail("profile@example.com");
        registerRequest.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(), AuthResponse.class);
        
        this.authToken = authResponse.getToken();
    }

    @Test
    void userProfileFlow_updateUsernameEmailPassword_success() throws Exception {
        // 1. Update username
        UpdateUsernameRequest usernameRequest = new UpdateUsernameRequest();
        usernameRequest.setNewUsername("updatedProfileUser");

        mockMvc.perform(put("/api/users/profile/username")
                        .with(csrf())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernameRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedProfileUser"));

        // 2. Update email
        UpdateEmailRequest emailRequest = new UpdateEmailRequest();
        emailRequest.setNewEmail("updated.profile@example.com");

        mockMvc.perform(put("/api/users/profile/email")
                        .with(csrf())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated.profile@example.com"));

        // 3. Update password
        UpdatePasswordRequest passwordRequest = new UpdatePasswordRequest();
        passwordRequest.setOldPassword("Password123!");
        passwordRequest.setNewPassword("NewPassword456!");

        mockMvc.perform(put("/api/users/profile/password")
                        .with(csrf())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk());

        // 4. Verify can login with new credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("updated.profile@example.com");
        loginRequest.setPassword("NewPassword456!");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userSearch_success() throws Exception {
        // Register another user to search for
        RegistrationRequest searchUser = new RegistrationRequest();
        searchUser.setUsername("searchableUser");
        searchUser.setEmail("searchable@example.com");
        searchUser.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchUser)))
                .andExpect(status().isCreated());

        // Search for users
        mockMvc.perform(get("/api/users/search")
                        .param("query", "searchable")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("searchableUser"));
    }
}