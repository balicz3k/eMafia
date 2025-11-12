package com.mafia.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.auth.*;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestApplicationConfig.class)
class UserManagementIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    RegistrationReq registerRequest = new RegistrationReq();
    registerRequest.setUsername("profileUser");
    registerRequest.setEmail("profile@example.com");
    registerRequest.setPassword("Password123!");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andReturn();

    AuthResp authResp =
        objectMapper.readValue(result.getResponse().getContentAsString(), AuthResp.class);

    this.authToken = authResp.getToken();
  }

  @Test
  void userProfileFlow_updateUsernameEmailPassword_success() throws Exception {

    UpdateUsernameReq usernameRequest = new UpdateUsernameReq();
    usernameRequest.setNewUsername("updatedProfileUser");

    mockMvc
        .perform(
            put("/api/users/profile/username")
                .with(csrf())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updatedProfileUser"));

    UpdateEmailReq emailRequest = new UpdateEmailReq();
    emailRequest.setNewEmail("updated.profile@example.com");

    mockMvc
        .perform(
            put("/api/users/profile/email")
                .with(csrf())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("updated.profile@example.com"));

    UpdatePasswordReq passwordRequest = new UpdatePasswordReq();
    passwordRequest.setOldPassword("Password123!");
    passwordRequest.setNewPassword("NewPassword456!");

    mockMvc
        .perform(
            put("/api/users/profile/password")
                .with(csrf())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
        .andExpect(status().isOk());

    LoginReq loginReq = new LoginReq();
    loginReq.setEmail("updated.profile@example.com");
    loginReq.setPassword("NewPassword456!");

    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk());
  }

  @Test
  void userSearch_success() throws Exception {

    RegistrationReq searchUser = new RegistrationReq();
    searchUser.setUsername("searchableUser");
    searchUser.setEmail("searchable@example.com");
    searchUser.setPassword("Password123!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchUser)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/users/search")
                .param("query", "searchable")
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("searchableUser"));
  }
}
