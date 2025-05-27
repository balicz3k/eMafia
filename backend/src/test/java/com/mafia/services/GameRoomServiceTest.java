package com.mafia.services;

import com.mafia.dto.GameRoomResponse;
import com.mafia.exceptions.UserNotFoundException;
import com.mafia.models.GameRoom;
import com.mafia.models.User;
import com.mafia.repositiories.GameRoomRepository;
import com.mafia.repositiories.PlayerInRoomRepository;
import com.mafia.repositiories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class GameRoomServiceTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerInRoomRepository playerInRoomRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameRoomService gameRoomService;

    private User mockUser;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testUser");

        authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getGameRoomsForCurrentUser_userFound_shouldReturnGameRoomResponses() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(UUID.randomUUID());
        gameRoom.setName("Test Room");
        gameRoom.setHost(mockUser);
        // Set other necessary fields for GameRoom and mapGameRoomToResponse
        when(gameRoomRepository.findGameRoomsByUser(mockUser)).thenReturn(Collections.singletonList(gameRoom));

        List<GameRoomResponse> responses = gameRoomService.getGameRoomsForCurrentUser();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(gameRoom.getName(), responses.get(0).getName());
        verify(userRepository, times(1)).findById(mockUser.getId());
        verify(gameRoomRepository, times(1)).findGameRoomsByUser(mockUser);
    }

    @Test
    void getGameRoomsForCurrentUser_userNotFound_shouldThrowUserNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> gameRoomService.getGameRoomsForCurrentUser());

        verify(userRepository, times(1)).findById(mockUser.getId());
        verify(gameRoomRepository, times(0)).findGameRoomsByUser(any(User.class));
    }

    @Test
    void searchGameRoomsByName_roomsFound_shouldReturnGameRoomResponses() {
        String searchTerm = "Test";
        GameRoom gameRoom = new GameRoom();
        gameRoom.setId(UUID.randomUUID());
        gameRoom.setName("Test Room");
        User host = new User();
        host.setId(UUID.randomUUID());
        host.setUsername("hostUser");
        gameRoom.setHost(host);
        // Set other necessary fields for GameRoom and mapGameRoomToResponse

        when(gameRoomRepository.findByNameContainingIgnoreCaseWithPlayers(searchTerm))
                .thenReturn(Collections.singletonList(gameRoom));

        List<GameRoomResponse> responses = gameRoomService.searchGameRoomsByName(searchTerm);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(gameRoom.getName(), responses.get(0).getName());
        verify(gameRoomRepository, times(1)).findByNameContainingIgnoreCaseWithPlayers(searchTerm);
    }

    @Test
    void searchGameRoomsByName_noRoomsFound_shouldReturnEmptyList() {
        String searchTerm = "NonExistent";
        when(gameRoomRepository.findByNameContainingIgnoreCaseWithPlayers(searchTerm)).thenReturn(Collections.emptyList());

        List<GameRoomResponse> responses = gameRoomService.searchGameRoomsByName(searchTerm);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(gameRoomRepository, times(1)).findByNameContainingIgnoreCaseWithPlayers(searchTerm);
    }
}
