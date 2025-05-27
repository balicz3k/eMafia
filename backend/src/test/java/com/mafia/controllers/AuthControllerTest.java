package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.RegistrationRequest;
import com.mafia.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test") // Użyj profilu testowego
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse createSampleAuthResponse() {
        return new AuthResponse("sample-jwt-token");
    }

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("newUser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");

        AuthResponse response = createSampleAuthResponse();

        when(userService.registerUser(any(RegistrationRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(response.getToken()));
    }

    @Test
    void register_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnAuthenticatedUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = createSampleAuthResponse();

        when(userService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(response.getToken()));
    }

    @Test
    void login_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withValidData_shouldCallUserService() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("validUser");
        request.setEmail("valid@example.com");
        request.setPassword("validPassword123");

        AuthResponse response = createSampleAuthResponse();

        when(userService.registerUser(any(RegistrationRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_withValidCredentials_shouldCallUserService() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("validPassword123");

        AuthResponse response = createSampleAuthResponse();

        when(userService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        // When
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}