package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig; // Added import
import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.LogoutRequest;
import com.mafia.dto.RegistrationRequest;
import com.mafia.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // Added import
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.mafia.components.JwtTokenProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestApplicationConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse createSampleAuthResponse() {
        return new AuthResponse("sample-jwt-token", "sample-refresh-token", 3600L);
    }

    @Test
    void register_shouldReturnCreatedUser() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        AuthResponse response = createSampleAuthResponse();
        when(userService.registerUser(any(RegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void register_withInvalidData_shouldReturnBadRequest() throws Exception {
        RegistrationRequest request = new RegistrationRequest();

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnAuthenticatedUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        AuthResponse response = createSampleAuthResponse();
        when(userService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_withValidCredentials_shouldCallUserService() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        AuthResponse response = createSampleAuthResponse();
        when(userService.authenticateUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_withInvalidData_shouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void logout_shouldReturnNoContent() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("sample-refresh-token-for-test");

        doNothing().when(userService).logoutUser(logoutRequest.getRefreshToken());

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))) // Send LogoutRequest in the body
                .andExpect(status().isNoContent());
    }
}