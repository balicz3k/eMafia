package com.mafia.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.databaseModels.GameRoom;
import com.mafia.databaseModels.PlayerInRoom;
import com.mafia.databaseModels.User;
import com.mafia.dto.StartGameRequest;
import com.mafia.dto.gameRoom.CreateGameRoomReq;
import com.mafia.enums.GameRoomStatus;
import com.mafia.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Testy integracyjne dla GameController.
 * 
 * Testują cały przepływ rozgrywki:
 * - Uruchamianie gry
 * - Pobieranie stanu gry
 * - Przechodzenie między fazami
 */
@AutoConfigureMockMvc
class GameIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private PlayerInRoomRepository playerInRoomRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User hostUser;
    private List<User> players;
    private List<String> playerTokens;
    private String hostToken;
    private GameRoom gameRoom;

    @BeforeEach
    void setUp() {
        // Tworzenie hosta
        hostUser = new User();
        hostUser.setUsername("host");
        hostUser.setEmail("host@example.com");
        hostUser.setPasswordHash("hashedpassword");
        hostUser.setAdmin(false);
        hostUser = userRepository.save(hostUser);
        hostToken = "Bearer " + jwtTokenProvider.generateToken(hostUser);

        // Tworzenie graczy (minimum 3 dla gry)
        players = new ArrayList<>();
        playerTokens = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User player = new User();
            player.setUsername("player" + i);
            player.setEmail("player" + i + "@example.com");
            player.setPasswordHash("hashedpassword");
            player.setAdmin(false);
            player = userRepository.save(player);
            players.add(player);
            playerTokens.add("Bearer " + jwtTokenProvider.generateToken(player));
        }

        // Tworzenie pokoju
        gameRoom = new GameRoom();
        gameRoom.setRoomCode("TEST01");
        gameRoom.setName("Test Game Room");
        gameRoom.setHost(hostUser);
        gameRoom.setMaxPlayers(10);
        gameRoom.setMafiaCount(1);
        gameRoom.setDiscussionTimeSeconds(120);
        gameRoom.setGameRoomStatus(GameRoomStatus.OPEN);
        gameRoom = gameRoomRepository.save(gameRoom);

        // Dodawanie hosta do pokoju
        PlayerInRoom hostInRoom = new PlayerInRoom();
        hostInRoom.setUser(hostUser);
        hostInRoom.setGameRoom(gameRoom);
        hostInRoom.setJoinedAt(LocalDateTime.now());
        playerInRoomRepository.save(hostInRoom);

        // Dodawanie graczy do pokoju
        for (User player : players) {
            PlayerInRoom playerInRoom = new PlayerInRoom();
            playerInRoom.setUser(player);
            playerInRoom.setGameRoom(gameRoom);
            playerInRoom.setJoinedAt(LocalDateTime.now());
            playerInRoomRepository.save(playerInRoom);
        }
    }

    @Nested
    @DisplayName("POST /api/games/start - Uruchamianie gry")
    class StartGameTests {

        @Test
        @DisplayName("Powinien uruchomić grę z wystarczającą liczbą graczy")
        void shouldStartGameWithEnoughPlayers() throws Exception {
            StartGameRequest request = new StartGameRequest();
            request.setRoomId(gameRoom.getId());
            request.setMafiaCount(1);
            request.setDiscussionTimeSeconds(60);

            mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.roomId").value(gameRoom.getId().toString()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.currentPhase").exists())
                    .andExpect(jsonPath("$.dayNumber").value(1));

            // Weryfikacja w bazie
            assert gameRepository.count() == 1;
        }

        @Test
        @DisplayName("Powinien zwrócić błąd dla nieistniejącego pokoju")
        void shouldReturnErrorForNonExistentRoom() throws Exception {
            StartGameRequest request = new StartGameRequest();
            request.setRoomId(UUID.randomUUID());
            request.setMafiaCount(1);
            request.setDiscussionTimeSeconds(60);

            mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Powinien zwrócić błąd dla za dużej liczby mafii")
        void shouldReturnErrorForTooManyMafia() throws Exception {
            StartGameRequest request = new StartGameRequest();
            request.setRoomId(gameRoom.getId());
            request.setMafiaCount(5); // Za dużo mafii dla 6 graczy
            request.setDiscussionTimeSeconds(60);

            mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/games/{gameId} - Pobieranie stanu gry")
    class GetGameStateTests {

        @Test
        @DisplayName("Powinien zwrócić stan istniejącej gry")
        void shouldReturnStateOfExistingGame() throws Exception {
            // Uruchamianie gry
            StartGameRequest startRequest = new StartGameRequest();
            startRequest.setRoomId(gameRoom.getId());
            startRequest.setMafiaCount(1);
            startRequest.setDiscussionTimeSeconds(60);

            MvcResult startResult = mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(startRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String gameId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                    .get("id").asText();

            // Pobieranie stanu gry
            mockMvc.perform(get("/api/games/" + gameId)
                    .header("Authorization", hostToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(gameId))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("Powinien zwrócić 404 dla nieistniejącej gry")
        void shouldReturn404ForNonExistentGame() throws Exception {
            mockMvc.perform(get("/api/games/" + UUID.randomUUID())
                    .header("Authorization", hostToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/games/rooms/{roomCode}/active-game - Aktywna gra w pokoju")
    class GetActiveGameTests {

        @Test
        @DisplayName("Powinien zwrócić aktywną grę dla pokoju")
        void shouldReturnActiveGameForRoom() throws Exception {
            // Uruchamianie gry
            StartGameRequest startRequest = new StartGameRequest();
            startRequest.setRoomId(gameRoom.getId());
            startRequest.setMafiaCount(1);
            startRequest.setDiscussionTimeSeconds(60);

            mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(startRequest)))
                    .andExpect(status().isOk());

            // Pobieranie aktywnej gry
            mockMvc.perform(get("/api/games/rooms/" + gameRoom.getRoomCode() + "/active-game")
                    .header("Authorization", hostToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomCode").value(gameRoom.getRoomCode()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.players").isArray())
                    .andExpect(jsonPath("$.players", hasSize(6))); // Host + 5 graczy
        }

        @Test
        @DisplayName("Powinien zwrócić 404 gdy brak aktywnej gry")
        void shouldReturn404WhenNoActiveGame() throws Exception {
            mockMvc.perform(get("/api/games/rooms/" + gameRoom.getRoomCode() + "/active-game")
                    .header("Authorization", hostToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/games/rooms/{roomCode}/me/role - Rola gracza")
    class GetPlayerRoleTests {

        @Test
        @DisplayName("Powinien zwrócić rolę gracza w grze")
        void shouldReturnPlayerRoleInGame() throws Exception {
            // Uruchamianie gry
            StartGameRequest startRequest = new StartGameRequest();
            startRequest.setRoomId(gameRoom.getId());
            startRequest.setMafiaCount(1);
            startRequest.setDiscussionTimeSeconds(60);

            mockMvc.perform(post("/api/games/start")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(startRequest)))
                    .andExpect(status().isOk());

            // Pobieranie roli gracza
            mockMvc.perform(get("/api/games/rooms/" + gameRoom.getRoomCode() + "/me/role")
                    .header("Authorization", hostToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").exists())
                    .andExpect(jsonPath("$.role").value(anyOf(is("CITIZEN"), is("MAFIA"))));
        }
    }
}
