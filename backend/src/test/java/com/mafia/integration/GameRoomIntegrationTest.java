package com.mafia.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.config.TestApplicationConfig;
import com.mafia.dto.auth.AuthResp;
import com.mafia.dto.auth.RegistrationReq;
import com.mafia.dto.gameRoom.CreateGameRoomReq;
import com.mafia.dto.gameRoom.CreateGameRoomResp;
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
class GameRoomIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    RegistrationReq registerRequest = new RegistrationReq();
    registerRequest.setUsername("gameUser");
    registerRequest.setEmail("gameuser@example.com");
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
  void gameRoomFlow_createRoom_success() throws Exception {
    CreateGameRoomReq createRequest = new CreateGameRoomReq();
    createRequest.setName("Integration Test Room");
    createRequest.setMaxPlayers(6);

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/gamerooms/create")
                    .with(csrf())
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Integration Test Room"))
            .andReturn();

    CreateGameRoomResp gameRoom =
        objectMapper.readValue(
            createResult.getResponse().getContentAsString(), CreateGameRoomResp.class);
    String roomCode = gameRoom.getRoomCode();

    mockMvc
        .perform(
            get("/api/gamerooms/{roomCode}", roomCode)
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomCode").value(roomCode))
        .andExpect(jsonPath("$.name").value("Integration Test Room"));
  }

  @Test
  void gameRoomFlow_unauthorizedAccess_shouldFail() throws Exception {
    CreateGameRoomReq createRequest = new CreateGameRoomReq();
    createRequest.setName("Unauthorized Test");
    createRequest.setMaxPlayers(4);

    mockMvc
        .perform(
            post("/api/gamerooms/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isForbidden());
  }
}
