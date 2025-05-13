package com.mafia.services;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.dto.GameRoomResponse;
import com.mafia.dto.PlayerInRoomResponse;
import com.mafia.exceptions.GameRoomNotFoundException;
import com.mafia.exceptions.RoomFullException;
import com.mafia.exceptions.UserAlreadyInRoomException;
import com.mafia.exceptions.UserNotFoundException;
import com.mafia.models.GameRoom;
import com.mafia.models.GameRoomStatus;
import com.mafia.models.PlayerInRoom;
import com.mafia.models.User;
import com.mafia.repositiories.GameRoomRepository;
import com.mafia.repositiories.PlayerInRoomRepository; // DODAJ IMPORT
import com.mafia.repositiories.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate; // DODAJ IMPORT
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor public class GameRoomService
{

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;
    private final PlayerInRoomRepository playerInRoomRepository; // WSTRZYKNIJ REPOZYTORIUM
    private final SimpMessagingTemplate messagingTemplate;       // WSTRZYKNIJ (do powiadomień WebSocket)

    @Value("${app.frontend.joinPath:/join/}") // Upewnij się, że masz to w application.properties
    private String frontendJoinPath;

    private static final String ROOM_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private String generateUniqueRoomCode()
    {
        StringBuilder sb;
        String code;
        do
        {
            sb = new StringBuilder(ROOM_CODE_LENGTH);
            for (int i = 0; i < ROOM_CODE_LENGTH; i++)
            {
                sb.append(ROOM_CODE_CHARACTERS.charAt(random.nextInt(ROOM_CODE_CHARACTERS.length())));
            }
            code = sb.toString();
        } while (gameRoomRepository.existsByRoomCode(code));
        return code;
    }

    // METODA POMOCNICZA DO MAPOWANIA GRACZA
    private PlayerInRoomResponse mapPlayerToResponse(PlayerInRoom player)
    {
        return new PlayerInRoomResponse(player.getId(), player.getUser().getId(), player.getUser().getUsername(),
                                        player.getNicknameInRoom(), player.isAlive(), player.getJoinedAt());
    }

    // ZAKTUALIZOWANA METODA POMOCNICZA DO MAPOWANIA POKOJU
    private GameRoomResponse mapGameRoomToResponse(GameRoom gameRoom)
    {
        List<PlayerInRoomResponse> playerResponses =
            gameRoom.getPlayers().stream().map(this::mapPlayerToResponse).collect(Collectors.toList());

        return new GameRoomResponse(gameRoom.getId(), gameRoom.getRoomCode(), gameRoom.getName(),
                                    gameRoom.getHost().getUsername(), gameRoom.getMaxPlayers(),
                                    gameRoom.getCurrentPlayersCount(), // Użyj metody z encji
                                    gameRoom.getStatus(), gameRoom.getCreatedAt(),
                                    frontendJoinPath, // To już było
                                    playerResponses);
    }

    @Transactional public GameRoomResponse createRoom(CreateGameRoomRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal(); // Rzutowanie na User
        User host =
            userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Host user not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = new GameRoom();
        gameRoom.setName(request.getName());
        gameRoom.setMaxPlayers(request.getMaxPlayers());
        gameRoom.setHost(host);
        gameRoom.setRoomCode(generateUniqueRoomCode());
        // gameRoom.setStatus(GameRoomStatus.WAITING_FOR_PLAYERS); // Ustawiane w konstruktorze GameRoom

        // Host automatycznie dołącza jako pierwszy gracz, używając swojego username jako nicku
        PlayerInRoom hostAsPlayer = new PlayerInRoom(host, gameRoom, host.getUsername());
        gameRoom.addPlayer(hostAsPlayer); // Dodaj hosta do listy graczy w pokoju

        GameRoom savedRoom = gameRoomRepository.save(gameRoom);
        // playerInRoomRepository.save(hostAsPlayer); // Niepotrzebne dzięki CascadeType.ALL w GameRoom

        return mapGameRoomToResponse(savedRoom);
    }

    // NOWA METODA DO DOŁĄCZANIA DO POKOJU
    @Transactional public GameRoomResponse joinRoom(String roomCode)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal();
        User userToJoin =
            userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + principalUser.getId()));

        GameRoom gameRoom =
            gameRoomRepository
                .findByRoomCodeWithPlayers(roomCode) // Użyj metody pobierającej z graczami
                .orElseThrow(() -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));

        if (gameRoom.getStatus() != GameRoomStatus.WAITING_FOR_PLAYERS)
        {
            throw new IllegalStateException("Cannot join room: Game is " +
                                            gameRoom.getStatus().name().toLowerCase().replace("_", " ") + ".");
        }

        if (playerInRoomRepository.existsByUserAndGameRoom(userToJoin, gameRoom))
        {
            throw new UserAlreadyInRoomException("User " + userToJoin.getUsername() + " is already in this room.");
        }

        if (gameRoom.getCurrentPlayersCount() >= gameRoom.getMaxPlayers())
        {
            throw new RoomFullException("Game room " + roomCode + " is full.");
        }

        // Użyj nazwy użytkownika jako nicku w pokoju
        PlayerInRoom newPlayer = new PlayerInRoom(userToJoin, gameRoom, userToJoin.getUsername());
        gameRoom.addPlayer(newPlayer);

        // Sprawdź, czy pokój jest pełny i zmień status, jeśli tak
        if (gameRoom.getCurrentPlayersCount() == gameRoom.getMaxPlayers())
        {
            gameRoom.setStatus(GameRoomStatus.READY_TO_START);
        }

        GameRoom updatedRoom =
            gameRoomRepository.save(gameRoom); // Zapisz pokój, aby zaktualizować listę graczy i status
        GameRoomResponse roomResponse = mapGameRoomToResponse(updatedRoom);

        // Powiadomienie WebSocket o nowym graczu (tylko informacja o graczu)
        messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/playerJoined",
                                         mapPlayerToResponse(newPlayer));
        // Powiadomienie o zaktualizowanym stanie całego pokoju (w tym status i lista graczy)
        messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/updated", roomResponse);

        return roomResponse;
    }

    // NOWA METODA DO POBIERANIA SZCZEGÓŁÓW POKOJU
    @Transactional(readOnly = true) public GameRoomResponse getRoomDetails(String roomCode)
    {
        GameRoom gameRoom =
            gameRoomRepository
                .findByRoomCodeWithPlayers(roomCode) // Użyj metody pobierającej z graczami
                .orElseThrow(() -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));
        return mapGameRoomToResponse(gameRoom);
    }
}