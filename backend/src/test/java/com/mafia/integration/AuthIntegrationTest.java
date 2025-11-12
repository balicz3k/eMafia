package com.mafia.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.auth.AuthResp;
import com.mafia.dto.auth.LoginReq;
import com.mafia.dto.auth.LogoutReq;
import com.mafia.dto.auth.RegistrationReq;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestApplicationConfig.class)
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void fullAuthFlow_registerLoginLogout_success() throws Exception {

    RegistrationReq registerRequest = new RegistrationReq();
    registerRequest.setUsername("integrationUser");
    registerRequest.setEmail("integration@example.com");
    registerRequest.setPassword("Password123!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andReturn();

    LoginReq loginReq = new LoginReq();
    loginReq.setEmail("integration@example.com");
    loginReq.setPassword("Password123!");

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andReturn();

    AuthResp loginResponse =
        objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResp.class);
    String accessToken = loginResponse.getToken();

    LogoutReq logoutReq = new LogoutReq(loginResponse.getRefreshToken());

    mockMvc
        .perform(
            post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutReq)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk());
  }

  @Test
  void authFlow_invalidCredentials_shouldFail() throws Exception {
    RegistrationReq tempRegisterRequest = new RegistrationReq();
    tempRegisterRequest.setUsername("userForInvalidLogin");
    tempRegisterRequest.setEmail("exists@example.com");
    tempRegisterRequest.setPassword("CorrectPassword123!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tempRegisterRequest)))
        .andExpect(status().isCreated());

    LoginReq invalidRequest = new LoginReq();
    invalidRequest.setEmail("exists@example.com");
    invalidRequest.setPassword("wrongpassword");

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authFlow_duplicateRegistration_shouldFail() throws Exception {
    RegistrationReq request = new RegistrationReq();
    request.setUsername("duplicateUser");
    request.setEmail("duplicate@example.com");
    request.setPassword("Password123!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }
}
