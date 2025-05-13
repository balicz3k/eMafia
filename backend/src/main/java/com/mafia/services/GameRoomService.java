package com.mafia.services;

import com.mafia.dto.CreateGameRoomRequest;
import com.mafia.dto.GameRoomResponse;
import com.mafia.exceptions.UserNotFoundException;
import com.mafia.models.GameRoom;
import com.mafia.models.GameRoomStatus;
import com.mafia.models.User;
import com.mafia.repositiories.GameRoomRepository;
import com.mafia.repositiories.UserRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor public class GameRoomService
{

    private final GameRoomRepository gameRoomRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend.joinPath:/join/}")
    // Domyślna ścieżka dołączania, można skonfigurować w application.properties
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

    @Transactional public GameRoomResponse createRoom(CreateGameRoomRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User principalUser = (User)authentication.getPrincipal(); // Zakładamy, że principal to obiekt User
        User host =
            userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new UserNotFoundException("Host user not found with ID: " + principalUser.getId()));

        GameRoom gameRoom = new GameRoom();
        gameRoom.setName(request.getName());
        gameRoom.setMaxPlayers(request.getMaxPlayers());
        gameRoom.setHost(host);
        gameRoom.setRoomCode(generateUniqueRoomCode());
        gameRoom.setStatus(GameRoomStatus.WAITING_FOR_PLAYERS); // Status jest już ustawiany w konstruktorze GameRoom
        // createdAt jest ustawiane przez @CreationTimestamp

        GameRoom savedRoom = gameRoomRepository.save(gameRoom);

        return new GameRoomResponse(savedRoom.getId(), savedRoom.getRoomCode(), savedRoom.getName(),
                                    savedRoom.getHost().getUsername(), savedRoom.getMaxPlayers(), savedRoom.getStatus(),
                                    savedRoom.getCreatedAt(), frontendJoinPath);
    }
}