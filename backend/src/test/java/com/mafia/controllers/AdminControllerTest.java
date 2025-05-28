package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.UpdateUserRolesRequest;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser(roles = "ADMIN")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse createSampleUserResponse(UUID userId, String username, Set<Role> roles) {
        return new UserResponse(userId, username, username + "@example.com", roles);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse sampleUser = createSampleUserResponse(userId, "testUser", Set.of(Role.ROLE_USER));
        List<UserResponse> userList = Collections.singletonList(sampleUser);

        when(userService.adminGetAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("testUser"))
                .andExpect(jsonPath("$[0].roles[0]").value(Role.ROLE_USER.name()));
    }

    @Test
    void updateUserRoles_shouldReturnUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Set<Role> newRolesEnum = Set.of(Role.ROLE_USER, Role.ROLE_ADMIN);
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(newRolesEnum);

        UserResponse updatedUserResponse = createSampleUserResponse(userId, "updatedUser", newRolesEnum);

        when(userService.adminUpdateUserRoles(eq(userId), eq(newRolesEnum))).thenReturn(updatedUserResponse);

        mockMvc.perform(put("/api/admin/users/{userId}/roles", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(
                        jsonPath("$.roles").value(newRolesEnum.stream().map(Enum::name).collect(Collectors.toList())));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(userService).adminDeleteUser(userId);

        mockMvc.perform(delete("/api/admin/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}