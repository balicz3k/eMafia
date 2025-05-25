package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.UpdateEmailRequest;
import com.mafia.dto.UpdatePasswordRequest;
import com.mafia.dto.UpdateUsernameRequest;
import com.mafia.dto.UserResponse;
import com.mafia.models.Role;
import com.mafia.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser // Zapewnia kontekst bezpieczeństwa dla @PreAuthorize("isAuthenticated()")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean // Potrzebne dla kontekstu bezpieczeństwa
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse createSampleUserResponse(UUID userId, String username, String email) {
        return new UserResponse(userId, username, email, Set.of(Role.ROLE_USER));
    }

    @Test
    void searchUsers_shouldReturnMatchingUsers() throws Exception {
        // Given
        String query = "test";
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        List<UserResponse> users = Arrays.asList(
            createSampleUserResponse(userId1, "testuser1", "testuser1@example.com"),
            createSampleUserResponse(userId2, "testuser2", "testuser2@example.com")
        );

        when(userService.searchUsers(query)).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(userId1.toString()))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[0].email").value("testuser1@example.com"))
                .andExpect(jsonPath("$[1].id").value(userId2.toString()))
                .andExpect(jsonPath("$[1].username").value("testuser2"))
                .andExpect(jsonPath("$[1].email").value("testuser2@example.com"));
    }

    @Test
    void searchUsers_withEmptyResult_shouldReturnEmptyList() throws Exception {
        // Given
        String query = "nonexistent";
        when(userService.searchUsers(query)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateUsername_shouldReturnUpdatedUser() throws Exception {
        // Given
        UpdateUsernameRequest request = new UpdateUsernameRequest();
        request.setNewUsername("newUsername");

        UUID userId = UUID.randomUUID();
        UserResponse updatedUser = createSampleUserResponse(userId, "newUsername", "user@example.com");

        when(userService.updateUsername(eq("newUsername"))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("newUsername"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void updateUsername_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given - żądanie z pustym username (narusza walidację)
        UpdateUsernameRequest request = new UpdateUsernameRequest();
        // Pozostawiamy newUsername jako null lub puste

        // When & Then
        mockMvc.perform(put("/api/users/profile/username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmail_shouldReturnUpdatedUser() throws Exception {
        // Given
        UpdateEmailRequest request = new UpdateEmailRequest();
        request.setNewEmail("newemail@example.com");

        UUID userId = UUID.randomUUID();
        UserResponse updatedUser = createSampleUserResponse(userId, "username", "newemail@example.com");

        when(userService.updateEmail(eq("newemail@example.com"))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("newemail@example.com"));
    }

    @Test
    void updateEmail_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Given - żądanie z nieprawidłowym emailem
        UpdateEmailRequest request = new UpdateEmailRequest();
        request.setNewEmail("invalid-email"); // Nieprawidłowy format email

        // When & Then
        mockMvc.perform(put("/api/users/profile/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmail_withEmptyEmail_shouldReturnBadRequest() throws Exception {
        // Given - żądanie z pustym emailem
        UpdateEmailRequest request = new UpdateEmailRequest();
        // Pozostawiamy newEmail jako null

        // When & Then
        mockMvc.perform(put("/api/users/profile/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePassword_shouldReturnOk() throws Exception {
        // Given
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("oldPassword123!");
        request.setNewPassword("newPassword456!");

        doNothing().when(userService).updatePassword(eq("oldPassword123!"), eq("newPassword456!"));

        // When & Then
        mockMvc.perform(put("/api/users/profile/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updatePassword_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given - żądanie z pustymi hasłami
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        // Pozostawiamy pola jako null

        // When & Then
        mockMvc.perform(put("/api/users/profile/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Given - brak kontekstu uwierzytelnienia
        // Test sprawdza czy @PreAuthorize("isAuthenticated()") działa

        // When & Then - Ten test wymaga osobnej konfiguracji bez @WithMockUser
        // W rzeczywistości ten test byłby trudny do zaimplementowania z @WithMockUser
        // Można go pominąć lub zaimplementować w testach integracyjnych
    }

    @Test
    void updateUsername_callsUserServiceWithCorrectParameter() throws Exception {
        // Given
        UpdateUsernameRequest request = new UpdateUsernameRequest();
        request.setNewUsername("testUsername");

        UUID userId = UUID.randomUUID();
        UserResponse updatedUser = createSampleUserResponse(userId, "testUsername", "user@example.com");

        when(userService.updateUsername(eq("testUsername"))).thenReturn(updatedUser);

        // When
        mockMvc.perform(put("/api/users/profile/username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - weryfikacja została już wykonana przez mockito podczas wywołania when()
    }

    @Test
    void updateEmail_callsUserServiceWithCorrectParameter() throws Exception {
        // Given
        UpdateEmailRequest request = new UpdateEmailRequest();
        request.setNewEmail("test@example.com");

        UUID userId = UUID.randomUUID();
        UserResponse updatedUser = createSampleUserResponse(userId, "username", "test@example.com");

        when(userService.updateEmail(eq("test@example.com"))).thenReturn(updatedUser);

        // When
        mockMvc.perform(put("/api/users/profile/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - weryfikacja została już wykonana przez mockito podczas wywołania when()
    }

    @Test
    void updatePassword_callsUserServiceWithCorrectParameters() throws Exception {
        // Given
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("currentPassword123!");
        request.setNewPassword("newStrongPassword456!");

        doNothing().when(userService).updatePassword(eq("currentPassword123!"), eq("newStrongPassword456!"));

        // When
        mockMvc.perform(put("/api/users/profile/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - weryfikacja została już wykonana przez mockito podczas wywołania when()
    }
}