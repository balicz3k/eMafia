package com.mafia.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.LogoutRequest;
import com.mafia.dto.RegistrationRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestApplicationConfig.class)
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void fullAuthFlow_registerLoginLogout_success() throws Exception {

                RegistrationRequest registerRequest = new RegistrationRequest();
                registerRequest.setUsername("integrationUser");
                registerRequest.setEmail("integration@example.com");
                registerRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.refreshToken").exists())
                                .andReturn();

                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("integration@example.com");
                loginRequest.setPassword("Password123!");

                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.refreshToken").exists())
                                .andReturn();

                AuthResponse loginResponse = objectMapper.readValue(
                                loginResult.getResponse().getContentAsString(), AuthResponse.class);
                String accessToken = loginResponse.getToken();

                LogoutRequest logoutRequest = new LogoutRequest(loginResponse.getRefreshToken());

                mockMvc.perform(post("/api/auth/logout")
                                .with(csrf())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(logoutRequest)))
                                .andExpect(status().isNoContent());

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk());
        }

        @Test
        void authFlow_invalidCredentials_shouldFail() throws Exception {
                RegistrationRequest tempRegisterRequest = new RegistrationRequest();
                tempRegisterRequest.setUsername("userForInvalidLogin");
                tempRegisterRequest.setEmail("exists@example.com");
                tempRegisterRequest.setPassword("CorrectPassword123!");

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tempRegisterRequest)))
                                .andExpect(status().isCreated());

                LoginRequest invalidRequest = new LoginRequest();
                invalidRequest.setEmail("exists@example.com");
                invalidRequest.setPassword("wrongpassword");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void authFlow_duplicateRegistration_shouldFail() throws Exception {
                RegistrationRequest request = new RegistrationRequest();
                request.setUsername("duplicateUser");
                request.setEmail("duplicate@example.com");
                request.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }
}