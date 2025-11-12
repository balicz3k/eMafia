package com.mafia.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.auth.AuthResp;
import com.mafia.dto.auth.LoginReq;
import com.mafia.dto.auth.LogoutReq;
import com.mafia.dto.auth.RegistrationReq;
import com.mafia.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestApplicationConfig.class)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @Autowired private ObjectMapper objectMapper;

  private AuthResp createSampleAuthResponse() {
    return new AuthResp("sample-jwt-token", "sample-refresh-token", 3600L);
  }

  @Test
  void register_shouldReturnCreatedUser() throws Exception {
    RegistrationReq request = new RegistrationReq();
    request.setUsername("testuser");
    request.setEmail("test@example.com");
    request.setPassword("Password123!");

    AuthResp response = createSampleAuthResponse();
    when(userService.registerUser(any(RegistrationReq.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void register_withInvalidData_shouldReturnBadRequest() throws Exception {
    RegistrationReq request = new RegistrationReq();

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldReturnAuthenticatedUser() throws Exception {
    LoginReq request = new LoginReq();
    request.setEmail("test@example.com");
    request.setPassword("Password123!");

    AuthResp response = createSampleAuthResponse();
    when(userService.authenticateUser(any(LoginReq.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void login_withValidCredentials_shouldCallUserService() throws Exception {
    LoginReq request = new LoginReq();
    request.setEmail("test@example.com");
    request.setPassword("Password123!");

    AuthResp response = createSampleAuthResponse();
    when(userService.authenticateUser(any(LoginReq.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.refreshToken").exists());
  }

  @Test
  void login_withInvalidData_shouldReturnBadRequest() throws Exception {
    LoginReq request = new LoginReq();

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void logout_shouldReturnNoContent() throws Exception {
    LogoutReq logoutReq = new LogoutReq("sample-refresh-token-for-test");

    doNothing().when(userService).logoutUser(logoutReq.getRefreshToken());

    mockMvc
        .perform(
            post("/api/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutReq)))
        .andExpect(status().isNoContent());
  }
}
