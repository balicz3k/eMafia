package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.databaseModels.User;
import com.mafia.dto.gameRoom.*;
import com.mafia.services.GameRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameRoomController.class)
public class GameRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRoomService gameRoomService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String roomCode;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        roomCode = "ABCD";

        // Mock authentication
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createGameRoom_ShouldReturnCreated() throws Exception {
        CreateGameRoomReq request = new CreateGameRoomReq();
        request.setName("Test Room");
        request.setMaxPlayers(10);

        CreateGameRoomResp response = new CreateGameRoomResp();
        response.setRoomCode(roomCode);
        response.setName("Test Room");

        when(gameRoomService.createRoom(any(CreateGameRoomReq.class))).thenReturn(response);

        mockMvc.perform(post("/api/game_rooms/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomCode").value(roomCode))
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    void joinGameRoom_ShouldReturnOk() throws Exception {
        JoinGameRoomResp response = new JoinGameRoomResp();
        response.setRoomCode(roomCode);
        response.setSuccess(true);

        when(gameRoomService.joinRoom(any(JoinGameRoomReq.class))).thenReturn(response);

        mockMvc.perform(post("/api/game_rooms/join/{roomCode}", roomCode)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value(roomCode))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getGameRoomDetails_ShouldReturnInfo() throws Exception {
        GameRoomInfoResp response = new GameRoomInfoResp();
        response.setRoomCode(roomCode);
        response.setName("Test Room");

        when(gameRoomService.getGameRoomInfoByCode(roomCode)).thenReturn(response);

        mockMvc.perform(get("/api/game_rooms/{roomCode}", roomCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value(roomCode))
                .andExpect(jsonPath("$.name").value("Test Room"));
    }

    @Test
    void getGameRoomsByFilter_ShouldReturnList() throws Exception {
        GameRoomInfoReq request = new GameRoomInfoReq();
        // Set valid fields if necessary for validation
        request.setPage(0);
        request.setSize(10);

        GameRoomListResp response = new GameRoomListResp();
        response.setRooms(Collections.emptyList());

        when(gameRoomService.getGameRoomsByFilter(any(GameRoomInfoReq.class))).thenReturn(response);

        mockMvc.perform(post("/api/game_rooms/info")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray());
    }

    @Test
    void searchGameRooms_ShouldReturnList() throws Exception {
        CreateGameRoomResp room = new CreateGameRoomResp();
        room.setRoomCode(roomCode);
        room.setName("Test Room");

        when(gameRoomService.searchGameRoomsByName("Test")).thenReturn(List.of(room));

        mockMvc.perform(get("/api/game_rooms/search")
                .param("name", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value(roomCode));
    }

    @Test
    void leaveGameRoom_ShouldReturnOk() throws Exception {
        LeaveGameRoomResp response = new LeaveGameRoomResp();
        response.setSuccess(true);

        when(gameRoomService.leaveRoom(any(LeaveGameRoomReq.class))).thenReturn(response);

        mockMvc.perform(post("/api/game_rooms/leave/{roomCode}", roomCode)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
