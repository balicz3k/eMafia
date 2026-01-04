package com.mafia.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.databaseModels.*;
import com.mafia.dto.StartGameRequest;
import com.mafia.dto.voting.CastVoteRequest;
import com.mafia.enums.*;
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
 * Testy integracyjne dla systemu głosowania.
 * 
 * Testują pełny przepływ głosowania:
 * - Tworzenie sesji głosowania
 * - Oddawanie głosów
 * - Pobieranie wyników
 */
@AutoConfigureMockMvc
class VotingIntegrationTest extends BaseIntegrationTest {

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
    private VotingSessionRepository votingSessionRepository;

    @Autowired
    private GameVoteRepository gameVoteRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User hostUser;
    private List<User> players;
    private List<String> playerTokens;
    private String hostToken;
    private GameRoom gameRoom;
    private UUID gameId;

    @BeforeEach
    void setUp() throws Exception {
        // Tworzenie użytkowników
        hostUser = createUser("host", "host@example.com");
        hostToken = "Bearer " + jwtTokenProvider.generateToken(hostUser);

        players = new ArrayList<>();
        playerTokens = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User player = createUser("player" + i, "player" + i + "@example.com");
            players.add(player);
            playerTokens.add("Bearer " + jwtTokenProvider.generateToken(player));
        }

        // Tworzenie pokoju gry
        gameRoom = createGameRoom("TEST01", "Voting Test Room", hostUser);

        // Dodawanie graczy do pokoju
        addPlayerToRoom(hostUser, gameRoom);
        for (User player : players) {
            addPlayerToRoom(player, gameRoom);
        }

        // Uruchamianie gry
        gameId = startGame();
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashedpassword");
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private GameRoom createGameRoom(String roomCode, String name, User host) {
        GameRoom room = new GameRoom();
        room.setRoomCode(roomCode);
        room.setName(name);
        room.setHost(host);
        room.setMaxPlayers(10);
        room.setMafiaCount(1);
        room.setDiscussionTimeSeconds(120);
        room.setGameRoomStatus(GameRoomStatus.OPEN);
        return gameRoomRepository.save(room);
    }

    private void addPlayerToRoom(User user, GameRoom room) {
        PlayerInRoom playerInRoom = new PlayerInRoom();
        playerInRoom.setUser(user);
        playerInRoom.setGameRoom(room);
        playerInRoom.setJoinedAt(LocalDateTime.now());
        playerInRoomRepository.save(playerInRoom);
    }

    private UUID startGame() throws Exception {
        StartGameRequest request = new StartGameRequest();
        request.setRoomId(gameRoom.getId());
        request.setMafiaCount(1);
        request.setDiscussionTimeSeconds(60);

        MvcResult result = mockMvc.perform(post("/api/games/start")
                .header("Authorization", hostToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(jsonNode.get("id").asText());
    }

    @Nested
    @DisplayName("GET /api/games/{gameId}/voting/current - Aktualna sesja głosowania")
    class GetCurrentVotingSessionTests {

        @Test
        @DisplayName("Powinien zwrócić 204 gdy brak aktywnej sesji")
        void shouldReturn204WhenNoActiveSession() throws Exception {
            // Manually close all active sessions for this game
            Game game = gameRepository.findById(gameId).orElseThrow();
            List<VotingSession> sessions = votingSessionRepository.findByGameOrderByCreatedAtDesc(game);
            for (VotingSession session : sessions) {
                if (session.getStatus() == VotingStatus.ACTIVE) {
                    session.setStatus(VotingStatus.COMPLETED);
                    votingSessionRepository.save(session);
                }
            }

            // Przed głosowaniem - gra może być w fazie nocnej
            mockMvc.perform(get("/api/games/" + gameId + "/voting/current")
                    .header("Authorization", hostToken))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/games/{gameId}/voting/vote - Oddawanie głosu")
    class CastVoteTests {

        @Test
        @DisplayName("Powinien zwrócić błąd gdy brak aktywnej sesji głosowania")
        void shouldReturnErrorWhenNoActiveVotingSession() throws Exception {
            CastVoteRequest request = new CastVoteRequest();
            request.setVotingSessionId(UUID.randomUUID());
            request.setTargetUserId(players.get(0).getId());

            mockMvc.perform(post("/api/games/" + gameId + "/voting/vote")
                    .header("Authorization", hostToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/games/{gameId}/voting/results - Wyniki głosowania")
    class GetVotingResultsTests {

        @Test
        @DisplayName("Powinien zwrócić 404 dla nieistniejącej sesji")
        void shouldReturn404ForNonExistentSession() throws Exception {
            mockMvc.perform(get("/api/games/" + gameId + "/voting/results")
                    .header("Authorization", hostToken)
                    .param("sessionId", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound());
        }
    }
}
