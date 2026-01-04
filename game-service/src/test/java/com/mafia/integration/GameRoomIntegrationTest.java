package com.mafia.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.components.JwtTokenProvider;
import com.mafia.databaseModels.User;
import com.mafia.dto.gameRoom.CreateGameRoomReq;
import com.mafia.repositories.GameRoomRepository;
import com.mafia.repositories.PlayerInRoomRepository;
import com.mafia.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Testy integracyjne dla GameRoomController.
 * 
 * Testy integracyjne różnią się od jednostkowych tym, że:
 * - Ładują pełny kontekst Spring (poprzez BaseIntegrationTest)
 * - Używają prawdziwej bazy danych PostgreSQL (Testcontainers)
 * - Testują cały przepływ: Controller -> Service -> Repository -> DB
 * - Weryfikują rzeczywiste zachowanie endpointów REST
 */
@AutoConfigureMockMvc
class GameRoomIntegrationTest extends BaseIntegrationTest {

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
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private User testUser2;
    private String authToken;
    private String authToken2;

    @BeforeEach
    void setUp() {
        // Tworzenie użytkownika testowego
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        // Tworzenie drugiego użytkownika do testów dołączania
        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPasswordHash("hashedpassword2");
        testUser2.setAdmin(false);
        testUser2 = userRepository.save(testUser2);

        // Generowanie tokenów JWT
        authToken = "Bearer " + jwtTokenProvider.generateToken(testUser);
        authToken2 = "Bearer " + jwtTokenProvider.generateToken(testUser2);
    }

    @Nested
    @DisplayName("POST /api/game_rooms/create - Tworzenie pokoju")
    class CreateGameRoomTests {

        @Test
        @DisplayName("Powinien utworzyć pokój gry z poprawnymi danymi")
        void shouldCreateGameRoomWithValidData() throws Exception {
            CreateGameRoomReq request = new CreateGameRoomReq();
            request.setName("Test Room");
            request.setMaxPlayers(6);

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.roomCode").isNotEmpty())
                    .andExpect(jsonPath("$.roomCode").value(hasLength(6)))
                    .andExpect(jsonPath("$.name").value("Test Room"));

            // Weryfikacja w bazie danych
            assert gameRoomRepository.count() == 1;
        }

        @Test
        @DisplayName("Powinien zwrócić 400 dla pustej nazwy pokoju")
        void shouldReturn400ForEmptyRoomName() throws Exception {
            CreateGameRoomReq request = new CreateGameRoomReq();
            request.setName("");
            request.setMaxPlayers(6);

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien zwrócić 400 dla za małej liczby graczy")
        void shouldReturn400ForTooFewPlayers() throws Exception {
            CreateGameRoomReq request = new CreateGameRoomReq();
            request.setName("Test Room");
            request.setMaxPlayers(2); // Minimum to 3

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien zwrócić 400 dla za dużej liczby graczy")
        void shouldReturn400ForTooManyPlayers() throws Exception {
            CreateGameRoomReq request = new CreateGameRoomReq();
            request.setName("Test Room");
            request.setMaxPlayers(25); // Maksimum to 20

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Powinien zwrócić 401 bez autoryzacji")
        void shouldReturn401WithoutAuthorization() throws Exception {
            CreateGameRoomReq request = new CreateGameRoomReq();
            request.setName("Test Room");
            request.setMaxPlayers(6);

            mockMvc.perform(post("/api/game_rooms/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/game_rooms/join/{roomCode} - Dołączanie do pokoju")
    class JoinGameRoomTests {

        @Test
        @DisplayName("Powinien pozwolić drugiemu użytkownikowi dołączyć do pokoju")
        void shouldAllowSecondUserToJoinRoom() throws Exception {
            // Najpierw tworzymy pokój
            CreateGameRoomReq createRequest = new CreateGameRoomReq();
            createRequest.setName("Join Test Room");
            createRequest.setMaxPlayers(6);

            MvcResult createResult = mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String roomCode = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("roomCode").asText();

            // Drugi użytkownik dołącza
            mockMvc.perform(post("/api/game_rooms/join/" + roomCode)
                    .header("Authorization", authToken2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomCode").value(roomCode));

            // Weryfikacja - 2 graczy w pokoju
            assert playerInRoomRepository.count() == 2;
        }

        @Test
        @DisplayName("Powinien zwrócić 404 dla nieistniejącego kodu pokoju")
        void shouldReturn404ForNonExistentRoomCode() throws Exception {
            mockMvc.perform(post("/api/game_rooms/join/XXXXXX")
                    .header("Authorization", authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/game_rooms/{roomCode} - Szczegóły pokoju")
    class GetGameRoomDetailsTests {

        @Test
        @DisplayName("Powinien zwrócić szczegóły istniejącego pokoju")
        void shouldReturnDetailsOfExistingRoom() throws Exception {
            // Tworzenie pokoju
            CreateGameRoomReq createRequest = new CreateGameRoomReq();
            createRequest.setName("Details Test Room");
            createRequest.setMaxPlayers(8);

            MvcResult createResult = mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String roomCode = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("roomCode").asText();

            // Pobieranie szczegółów
            mockMvc.perform(get("/api/game_rooms/" + roomCode)
                    .header("Authorization", authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomCode").value(roomCode))
                    .andExpect(jsonPath("$.name").value("Details Test Room"))
                    .andExpect(jsonPath("$.maxPlayers").value(8))
                    .andExpect(jsonPath("$.players").isArray())
                    .andExpect(jsonPath("$.players", hasSize(1))); // Host jest w pokoju
        }

        @Test
        @DisplayName("Powinien zwrócić 404 dla nieistniejącego pokoju")
        void shouldReturn404ForNonExistentRoom() throws Exception {
            mockMvc.perform(get("/api/game_rooms/YYYYYY")
                    .header("Authorization", authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/game_rooms/leave/{roomCode} - Opuszczanie pokoju")
    class LeaveGameRoomTests {

        @Test
        @DisplayName("Powinien pozwolić użytkownikowi opuścić pokój")
        void shouldAllowUserToLeaveRoom() throws Exception {
            // Tworzenie pokoju
            CreateGameRoomReq createRequest = new CreateGameRoomReq();
            createRequest.setName("Leave Test Room");
            createRequest.setMaxPlayers(6);

            MvcResult createResult = mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String roomCode = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("roomCode").asText();

            // Drugi użytkownik dołącza
            mockMvc.perform(post("/api/game_rooms/join/" + roomCode)
                    .header("Authorization", authToken2))
                    .andExpect(status().isOk());

            // Drugi użytkownik opuszcza
            mockMvc.perform(post("/api/game_rooms/leave/" + roomCode)
                    .header("Authorization", authToken2))
                    .andExpect(status().isOk());

            // Weryfikacja - tylko host pozostał
            assert playerInRoomRepository.count() == 1;
        }
    }

    @Nested
    @DisplayName("GET /api/game_rooms/search - Wyszukiwanie pokoi")
    class SearchGameRoomsTests {

        @Test
        @DisplayName("Powinien znaleźć pokoje po nazwie")
        void shouldFindRoomsByName() throws Exception {
            // Tworzenie pokoi
            CreateGameRoomReq request1 = new CreateGameRoomReq();
            request1.setName("Mafia Night");
            request1.setMaxPlayers(6);

            CreateGameRoomReq request2 = new CreateGameRoomReq();
            request2.setName("Fun Game");
            request2.setMaxPlayers(8);

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/game_rooms/create")
                    .header("Authorization", authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isCreated());

            // Wyszukiwanie
            mockMvc.perform(get("/api/game_rooms/search")
                    .header("Authorization", authToken)
                    .param("name", "Mafia"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("Mafia Night"));
        }

        @Test
        @DisplayName("Powinien zwrócić pustą listę gdy brak wyników")
        void shouldReturnEmptyListWhenNoResults() throws Exception {
            mockMvc.perform(get("/api/game_rooms/search")
                    .header("Authorization", authToken)
                    .param("name", "NonExistentRoom"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
