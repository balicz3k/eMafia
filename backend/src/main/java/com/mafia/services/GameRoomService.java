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

        return new GameRoomResponse(gameRoom.getId(), gameRoom.getRoomCode(), gameRoom.getName(),
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
}