package com.mafia.services;

import com.mafia.exceptions.ForbiddenActionException;
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
import com.mafia.repositiories.PlayerInRoomRepository;
import com.mafia.repositiories.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor public class GameRoomService
{

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;
    private final PlayerInRoomRepository playerInRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.frontend.joinPath:/join/}") private String frontendJoinPath;

    private static final String ROOM_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public List<GameRoomResponse> getGameRoomsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User) authentication.getPrincipal();
        User currentUser = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + principalUser.getId()));

        List<GameRoom> gameRooms = gameRoomRepository.findGameRoomsByUser(currentUser);
        return gameRooms.stream()
                .map(this::mapGameRoomToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GameRoomResponse> searchGameRoomsByName(String name) {
        List<GameRoom> gameRooms = gameRoomRepository.findByNameContainingIgnoreCaseWithPlayers(name);
        return gameRooms.stream()
                .map(this::mapGameRoomToResponse)
                .collect(Collectors.toList());
    }

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

    private PlayerInRoomResponse mapPlayerToResponse(PlayerInRoom player)
    {
        return new PlayerInRoomResponse(player.getId(), player.getUser().getId(), player.getUser().getUsername(),
                                        player.getNicknameInRoom(), player.isAlive(), player.getJoinedAt());
    }

    private GameRoomResponse mapGameRoomToResponse(GameRoom gameRoom)
    {
        List<PlayerInRoomResponse> playerResponses =
            gameRoom.getPlayers().stream().map(this::mapPlayerToResponse).collect(Collectors.toList());

        return new GameRoomResponse(gameRoom.getId(), gameRoom.getRoomCode(), gameRoom.getName(), gameRoom.getHost().getId(),
                                    gameRoom.getHost().getUsername(), gameRoom.getMaxPlayers(),
                                    gameRoom.getCurrentPlayersCount(), gameRoom.getStatus(), gameRoom.getCreatedAt(),
                                    frontendJoinPath, playerResponses);
    }

    @Transactional public GameRoomResponse createRoom(CreateGameRoomRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal();
        User host =
            userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Host user not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = new GameRoom();
        gameRoom.setName(request.getName());
        gameRoom.setMaxPlayers(request.getMaxPlayers());
        gameRoom.setHost(host);
        gameRoom.setRoomCode(generateUniqueRoomCode());

        PlayerInRoom hostAsPlayer = new PlayerInRoom(host, gameRoom, host.getUsername());
        gameRoom.addPlayer(hostAsPlayer);

        GameRoom savedRoom = gameRoomRepository.save(gameRoom);

        return mapGameRoomToResponse(savedRoom);
    }

    @Transactional public GameRoomResponse joinRoom(String roomCode)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal();
        User userToJoin =
            userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = gameRoomRepository.findByRoomCodeWithPlayers(roomCode).orElseThrow(
            () -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));

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

        PlayerInRoom newPlayer = new PlayerInRoom(userToJoin, gameRoom, userToJoin.getUsername());
        gameRoom.addPlayer(newPlayer);

        if (gameRoom.getCurrentPlayersCount() == gameRoom.getMaxPlayers())
        {
            gameRoom.setStatus(GameRoomStatus.READY_TO_START);
        }

        GameRoom updatedRoom = gameRoomRepository.save(gameRoom);
        GameRoomResponse roomResponse = mapGameRoomToResponse(updatedRoom);

        messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/playerJoined",
                                         mapPlayerToResponse(newPlayer));
        messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/updated", roomResponse);

        return roomResponse;
    }

    @Transactional(readOnly = true) public GameRoomResponse getRoomDetails(String roomCode)
    {
        GameRoom gameRoom = gameRoomRepository.findByRoomCodeWithPlayers(roomCode).orElseThrow(
            () -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));
        return mapGameRoomToResponse(gameRoom);
    }

       @Transactional
    public void leaveRoom(String roomCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User) authentication.getPrincipal();
        User currentUser = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = gameRoomRepository.findByRoomCodeWithPlayers(roomCode)
                .orElseThrow(() -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));

        PlayerInRoom playerToRemove = gameRoom.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User is not in this room."));

        // Nie pozwalamy na opuszczanie pokoi, które są już formalnie zakończone lub anulowane,
        // chyba że logika biznesowa tego wymaga (np. usunięcie z historii gracza).
        // Na potrzeby tej logiki, jeśli gra jest FINISHED/CANCELLED, nie robimy nic.
        if (gameRoom.getStatus() == GameRoomStatus.FINISHED || gameRoom.getStatus() == GameRoomStatus.ABANDONED) {
            // Można by tu dodać logikę, jeśli np. chcemy p dozwolić na "usunięcie się" z listy zakończonych gier.
            // Na razie, jeśli gra jest zakończona, nie ma akcji "opuszczania".
            return;
        }

        boolean wasHost = gameRoom.getHost().getId().equals(currentUser.getId());

        if (wasHost) {
            // Host opuszcza pokój - usuwamy cały pokój.
            // Dzięki CascadeType.ALL i orphanRemoval=true na GameRoom.players,
            // wszystkie powiązane PlayerInRoom zostaną również usunięte.
            gameRoomRepository.delete(gameRoom);

            // Wyślij informację do wszystkich (potencjalnych) graczy, że pokój został usunięty/zakończony.
            // Można stworzyć dedykowany DTO dla tego typu wiadomości.
            // Dla uproszczenia, wysyłamy informację o anulowaniu, chociaż pokój jest usuwany.
            // Klienci powinni obsłużyć brak pokoju przy następnym odświeżeniu lub próbie interakcji.
            messagingTemplate.convertAndSend("/topic/game/" + gameRoom.getRoomCode() + "/roomDeleted",
                                             "Room " + gameRoom.getName() + " has been deleted by the host.");
            // Alternatywnie, można wysłać zaktualizowany status jako CANCELLED tuż przed usunięciem,
            // ale usunięcie jest bardziej definitywne.
        } else {
            // Zwykły gracz opuszcza pokój.
            gameRoom.removePlayer(playerToRemove); // To usunie PlayerInRoom dzięki orphanRemoval=true

            // Zaktualizuj status pokoju, jeśli to konieczne
            if (gameRoom.getPlayers().isEmpty() && gameRoom.getStatus() != GameRoomStatus.WAITING_FOR_PLAYERS) {
                 // Jeśli pokój stał się pusty (a nie był to host, który by go usunął)
                 // Można by go anulować lub zostawić, zależy od logiki.
                 // Na razie zostawiamy, bo host mógłby jeszcze wrócić lub zaprosić kogoś.
                 // Jeśli jednak chcemy anulować pusty pokój:
                 // gameRoom.setStatus(GameRoomStatus.CANCELLED);
            } else if (gameRoom.getStatus() == GameRoomStatus.READY_TO_START && gameRoom.getPlayers().size() < gameRoom.getMaxPlayers()) {
                // Jeśli pokój był gotowy do startu, a teraz brakuje graczy
                gameRoom.setStatus(GameRoomStatus.WAITING_FOR_PLAYERS);
            }
            // Jeśli pokój był IN_PROGRESS, opuszczenie przez gracza może wymagać innej logiki
            // (np. oznaczenie gracza jako "opuścił", ale gra toczy się dalej, jeśli to możliwe).
            // Obecna logika nie obsługuje jeszcze stanu IN_PROGRESS w kontekście opuszczania.

            GameRoom updatedRoom = gameRoomRepository.save(gameRoom);
            GameRoomResponse roomResponse = mapGameRoomToResponse(updatedRoom);

            // Wyślij informację o opuszczeniu gracza i zaktualizowanym stanie pokoju
            messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/playerLeft", mapPlayerToResponse(playerToRemove));
            messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/updated", roomResponse);
        }
    }

    @Transactional
    public void endRoom(String roomCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User) authentication.getPrincipal();
        User currentUser = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = gameRoomRepository.findByRoomCodeWithPlayers(roomCode) // Pobierz z graczami dla mapGameRoomToResponse
                .orElseThrow(() -> new GameRoomNotFoundException("Game room not found with code: " + roomCode));

        // Tylko host może zakończyć grę przez ten endpoint
        if (!gameRoom.getHost().getId().equals(currentUser.getId())) {
            throw new ForbiddenActionException("Only the host can end the game.");
        }

        if (gameRoom.getStatus() == GameRoomStatus.FINISHED || gameRoom.getStatus() == GameRoomStatus.ABANDONED /* lub ABANDONED, jeśli używasz */) {
            // Gra już zakończona, nie rób nic lub zwróć informację
            return;
        }

        gameRoom.setStatus(GameRoomStatus.FINISHED); // Lub CANCELLED/ABANDONED w zależności od logiki
        GameRoom updatedRoom = gameRoomRepository.save(gameRoom);
        GameRoomResponse roomResponse = mapGameRoomToResponse(updatedRoom);

        messagingTemplate.convertAndSend("/topic/game/" + updatedRoom.getRoomCode() + "/updated", roomResponse);
    }
}