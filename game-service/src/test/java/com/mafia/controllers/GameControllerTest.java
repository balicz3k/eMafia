package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.databaseModels.User;
import com.mafia.dto.GameStateResponse;
import com.mafia.dto.GameWithPlayersDto;
import com.mafia.dto.PlayerRoleDto;
import com.mafia.dto.StartGameRequest;
import com.mafia.enums.GamePhase;
import com.mafia.enums.GameStatus;
import com.mafia.enums.Role;
import com.mafia.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UUID gameId;
    private String roomCode;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        gameId = UUID.randomUUID();
        roomCode = "ABCD";

        // Mock authentication
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void start_ShouldReturnGameStateResponse() throws Exception {
        StartGameRequest request = new StartGameRequest();
        request.setRoomCode(roomCode);

        GameStateResponse response = new GameStateResponse();
        response.setGameId(gameId);
        response.setStatus(GameStatus.IN_PROGRESS);

        when(gameService.startGame(any(StartGameRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/games/start")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void get_ShouldReturnGameStateResponse() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId(gameId);
        response.setPhase(GamePhase.DAY);

        when(gameService.getState(gameId)).thenReturn(response);

        mockMvc.perform(get("/api/games/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.phase").value("DAY"));
    }

    @Test
    void advance_ShouldReturnUpdatedGameState() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId(gameId);
        response.setPhase(GamePhase.NIGHT);

        when(gameService.advancePhase(gameId)).thenReturn(response);

        mockMvc.perform(post("/api/games/{gameId}/advance-phase", gameId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.phase").value("NIGHT"));
    }

    @Test
    void end_ShouldReturnEndedGameState() throws Exception {
        GameStateResponse response = new GameStateResponse();
        response.setGameId(gameId);
        response.setStatus(GameStatus.FINISHED);

        when(gameService.endGame(gameId)).thenReturn(response);

        mockMvc.perform(post("/api/games/{gameId}/end", gameId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    void getActiveGame_ShouldReturnGameWithPlayersDto() throws Exception {
        GameWithPlayersDto gameDto = new GameWithPlayersDto();
        gameDto.setId(gameId);
        gameDto.setStatus(GameStatus.IN_PROGRESS);

        when(gameService.getActiveGameByRoomCode(eq(roomCode), any(UUID.class))).thenReturn(gameDto);

        mockMvc.perform(get("/api/games/rooms/{roomCode}/active-game", roomCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getMyRole_ShouldReturnPlayerRoleDto() throws Exception {
        PlayerRoleDto roleDto = new PlayerRoleDto();
        roleDto.setRole(Role.MAFIA);

        when(gameService.getPlayerRole(eq(roomCode), any(UUID.class))).thenReturn(roleDto);

        mockMvc.perform(get("/api/games/rooms/{roomCode}/me/role", roomCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MAFIA"));
    }
}
