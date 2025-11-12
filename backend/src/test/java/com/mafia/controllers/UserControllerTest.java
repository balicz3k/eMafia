package com.mafia.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.auth.UpdateEmailReq;
import com.mafia.dto.auth.UpdatePasswordReq;
import com.mafia.dto.auth.UpdateUsernameReq;
import com.mafia.dto.auth.UserInfoResp;
import com.mafia.enums.Role;
import com.mafia.services.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @Autowired private ObjectMapper objectMapper;

  private UserInfoResp sampleUserInfoResp;
  private UUID sampleUserId;

  @BeforeEach
  void setUp() {
    sampleUserId = UUID.randomUUID();
    sampleUserInfoResp =
        new UserInfoResp(sampleUserId, "testUser", "test@example.com", Set.of(Role.ROLE_USER));
  }

  @Test
  @WithMockUser
  void searchUsers_whenAuthenticatedAndQueryProvided_shouldReturnUserList() throws Exception {
    String query = "test";
    List<UserInfoResp> userList = Collections.singletonList(sampleUserInfoResp);
    when(userService.searchUsers(query)).thenReturn(userList);

    mockMvc
        .perform(get("/api/users/search").param("query", query).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(sampleUserId.toString()))
        .andExpect(jsonPath("$[0].username").value(sampleUserInfoResp.getUsername()))
        .andExpect(jsonPath("$[0].email").value(sampleUserInfoResp.getEmail()));
  }

  @Test
  void searchUsers_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/users/search").param("query", "test").with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updateUsername_whenAuthenticatedAndValidRequest_shouldReturnUpdatedUser() throws Exception {
    UpdateUsernameReq request = new UpdateUsernameReq();
    request.setNewUsername("newTestUser");

    UserInfoResp updatedUserInfoResp =
        new UserInfoResp(
            sampleUserId,
            request.getNewUsername(),
            sampleUserInfoResp.getEmail(),
            Set.of(Role.ROLE_USER));
    when(userService.updateUsername(request.getNewUsername())).thenReturn(updatedUserInfoResp);

    mockMvc
        .perform(
            put("/api/users/profile/username")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(sampleUserId.toString()))
        .andExpect(jsonPath("$.username").value(request.getNewUsername()));
  }

  @Test
  void updateUsername_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    UpdateUsernameReq request = new UpdateUsernameReq();
    request.setNewUsername("newTestUser");

    mockMvc
        .perform(
            put("/api/users/profile/username")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updateUsername_whenAuthenticatedAndInvalidRequest_shouldReturnBadRequest() throws Exception {
    UpdateUsernameReq request = new UpdateUsernameReq();

    mockMvc
        .perform(
            put("/api/users/profile/username")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void updateEmail_whenAuthenticatedAndValidRequest_shouldReturnUpdatedUser() throws Exception {
    UpdateEmailReq request = new UpdateEmailReq();
    request.setNewEmail("newemail@example.com");

    UserInfoResp updatedUserInfoResp =
        new UserInfoResp(
            sampleUserId,
            sampleUserInfoResp.getUsername(),
            request.getNewEmail(),
            Set.of(Role.ROLE_USER));
    when(userService.updateEmail(request.getNewEmail())).thenReturn(updatedUserInfoResp);

    mockMvc
        .perform(
            put("/api/users/profile/email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(sampleUserId.toString()))
        .andExpect(jsonPath("$.email").value(request.getNewEmail()));
  }

  @Test
  void updateEmail_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    UpdateEmailReq request = new UpdateEmailReq();
    request.setNewEmail("newemail@example.com");

    mockMvc
        .perform(
            put("/api/users/profile/email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updateEmail_whenAuthenticatedAndInvalidRequest_shouldReturnBadRequest() throws Exception {
    UpdateEmailReq request = new UpdateEmailReq();
    request.setNewEmail("invalid-email");

    mockMvc
        .perform(
            put("/api/users/profile/email")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void updatePassword_whenAuthenticatedAndValidRequest_shouldReturnOk() throws Exception {
    UpdatePasswordReq request = new UpdatePasswordReq();
    request.setOldPassword("oldPassword123");
    request.setNewPassword("newValidPassword123!");

    doNothing()
        .when(userService)
        .updatePassword(request.getOldPassword(), request.getNewPassword());

    mockMvc
        .perform(
            put("/api/users/profile/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updatePassword_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
    UpdatePasswordReq request = new UpdatePasswordReq();
    request.setOldPassword("oldPassword123");
    request.setNewPassword("newValidPassword123!");

    mockMvc
        .perform(
            put("/api/users/profile/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void updatePassword_whenAuthenticatedAndInvalidRequest_shouldReturnBadRequest() throws Exception {
    UpdatePasswordReq request = new UpdatePasswordReq();

    mockMvc
        .perform(
            put("/api/users/profile/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
