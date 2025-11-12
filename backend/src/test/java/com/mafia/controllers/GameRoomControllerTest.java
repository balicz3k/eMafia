package com.mafia.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.gameRoom.CreateGameRoomReq;
import com.mafia.dto.gameRoom.CreateGameRoomResp;
import com.mafia.enums.GameRoomStatus;
import com.mafia.services.GameRoomService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameRoomController.class)
@WithMockUser
public class GameRoomControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GameRoomService gameRoomService;

  @MockBean private JwtTokenProvider jwtTokenProvider;

  @Autowired private ObjectMapper objectMapper;

  private CreateGameRoomResp createSampleGameRoomResponse() {
    return new CreateGameRoomResp(
        UUID.randomUUID(),
        "ROOM123",
        "Test Room",
        UUID.randomUUID(),
        "HostUser",
        10,
        1,
        GameRoomStatus.WAITING_FOR_PLAYERS,
        LocalDateTime.now(),
        "/join/ROOM123",
        Collections.emptyList());
  }

  @Test
  void createGameRoom_shouldReturnCreatedRoom() throws Exception {
    CreateGameRoomReq request = new CreateGameRoomReq();
    request.setName("Test Room");
    request.setMaxPlayers(10);

    CreateGameRoomResp response = createSampleGameRoomResponse();
    when(gameRoomService.createRoom(any(CreateGameRoomReq.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/game_rooms/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.roomCode").value(response.getRoomCode()))
        .andExpect(jsonPath("$.name").value(response.getName()));
  }

  @Test
  void createGameRoom_withInvalidData_shouldReturnBadRequest() throws Exception {
    CreateGameRoomReq request = new CreateGameRoomReq();

    mockMvc
        .perform(
            post("/api/game_rooms/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void joinGameRoom_shouldReturnJoinedRoom() throws Exception {
    String roomCode = "ROOM123";
    CreateGameRoomResp response = createSampleGameRoomResponse();
    when(gameRoomService.joinRoom(roomCode)).thenReturn(response);

    mockMvc
        .perform(post("/api/gamerooms/{roomCode}/join", roomCode).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.roomCode").value(roomCode));
  }

  @Test
  void joinGameRoom_invalidCode_shouldReturnNotFound() throws Exception {
    String roomCode = "INVALID";
    when(gameRoomService.joinRoom(roomCode)).thenThrow(new RuntimeException("Room not found"));

    mockMvc
        .perform(post("/api/gamerooms/{roomCode}/join", roomCode).with(csrf()))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getGameRoomDetails_shouldReturnRoomDetails() throws Exception {
    String roomCode = "ROOM123";
    CreateGameRoomResp response = createSampleGameRoomResponse();
    when(gameRoomService.getRoomDetails(roomCode)).thenReturn(response);

    mockMvc
        .perform(get("/api/gamerooms/{roomCode}", roomCode))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.roomCode").value(roomCode));
  }

  @Test
  void getMyGameRooms_shouldReturnUserRooms() throws Exception {
    List<CreateGameRoomResp> responses = Collections.singletonList(createSampleGameRoomResponse());
    when(gameRoomService.getGameRoomsForCurrentUser()).thenReturn(responses);

    mockMvc
        .perform(get("/api/gamerooms/my-rooms"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].roomCode").value(responses.get(0).getRoomCode()));
  }

  @Test
  void searchGameRooms_shouldReturnMatchingRooms() throws Exception {
    String query = "Test";
    List<CreateGameRoomResp> responses = Collections.singletonList(createSampleGameRoomResponse());
    when(gameRoomService.searchGameRoomsByName(query)).thenReturn(responses);

    mockMvc
        .perform(get("/api/gamerooms/search").param("name", query))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].name").value(responses.get(0).getName()));
  }

  @Test
  void searchGameRooms_withoutQuery_shouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/api/gamerooms/search")).andExpect(status().isBadRequest());
  }

  @Test
  void leaveGameRoom_shouldReturnNoContent() throws Exception {
    String roomCode = "ROOM123";
    doNothing().when(gameRoomService).leaveRoom(roomCode);

    mockMvc
        .perform(post("/api/gamerooms/{roomCode}/leave", roomCode).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void endRoom_shouldReturnNoContent() throws Exception {
    String roomCode = "ROOM123";
    doNothing().when(gameRoomService).endRoom(roomCode);

    mockMvc
        .perform(post("/api/gamerooms/{roomCode}/end", roomCode).with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void endRoom_notHost_shouldReturnForbidden() throws Exception {
    String roomCode = "ROOM123";
    doThrow(new RuntimeException("Not authorized")).when(gameRoomService).endRoom(roomCode);

    mockMvc
        .perform(post("/api/gamerooms/{roomCode}/end", roomCode).with(csrf()))
        .andExpect(status().isInternalServerError());
  }
}
