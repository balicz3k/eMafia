package com.mafia.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.databaseModels.User;
import com.mafia.dto.auth.AuthResp;
import com.mafia.dto.auth.LoginReq;
import com.mafia.dto.auth.RegistrationReq;
import com.mafia.enums.Role;
import com.mafia.repositories.UserRepository;
import java.util.HashSet;
import java.util.Set;
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
class AdminIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  private String adminToken;
  private String regularUserToken;

  @BeforeEach
  void setUp() throws Exception {

    RegistrationReq userRequest = new RegistrationReq();
    userRequest.setUsername("regularUser");
    userRequest.setEmail("regular@example.com");
    userRequest.setPassword("Password123!");

    MvcResult userResult =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated())
            .andReturn();

    AuthResp userAuth =
        objectMapper.readValue(userResult.getResponse().getContentAsString(), AuthResp.class);
    this.regularUserToken = userAuth.getToken();

    RegistrationReq adminCandidateRequest = new RegistrationReq();
    adminCandidateRequest.setUsername("adminUser");
    adminCandidateRequest.setEmail("admin@example.com");
    adminCandidateRequest.setPassword("AdminPass123!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminCandidateRequest)))
        .andExpect(status().isCreated())
        .andReturn();

    User adminUser =
        userRepository
            .findByEmail("admin@example.com")
            .orElseThrow(
                () ->
                    new IllegalStateException("Admin candidate user not found after registration"));
    adminUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER)));

    userRepository.save(adminUser);

    LoginReq adminLoginReq = new LoginReq();
    adminLoginReq.setEmail("admin@example.com");
    adminLoginReq.setPassword("AdminPass123!");

    MvcResult adminLoginResult =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminLoginReq)))
            .andExpect(status().isOk())
            .andReturn();

    AuthResp adminAuthResp =
        objectMapper.readValue(adminLoginResult.getResponse().getContentAsString(), AuthResp.class);
    this.adminToken = adminAuthResp.getToken();
  }

  @Test
  void adminFlow_manageUsers_requiresAdminRole() throws Exception {

    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + regularUserToken))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void adminFlow_getAllUsers_success() throws Exception {
    mockMvc
        .perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void adminFlow_updateUserRoles_success() throws Exception {
    User regularUser = userRepository.findByEmail("regular@example.com").orElseThrow();

    UpdateUserRolesRequest request = new UpdateUserRolesRequest();
    request.setRoles(Set.of(Role.ROLE_ADMIN));

    mockMvc
        .perform(
            put("/api/admin/users/{userId}/roles", regularUser.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.roles").value(org.hamcrest.Matchers.containsInAnyOrder("ROLE_ADMIN")));
  }
}
