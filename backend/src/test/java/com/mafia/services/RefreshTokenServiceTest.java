package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.AuthResponse;
import com.mafia.exceptions.TokenExpiredException;
import com.mafia.exceptions.TokenNotFoundException;
import com.mafia.models.RefreshToken;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(Collections.singleton(Role.ROLE_USER));

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("test-refresh-token");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        testRefreshToken.setRevoked(false);
    }

    @Test
    void createRefreshToken_success() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);
        when(refreshTokenRepository.countValidTokensByUser(any(User.class), any(LocalDateTime.class))).thenReturn(0L);

        RefreshToken result = refreshTokenService.createRefreshToken(testUser, "Login");

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertFalse(result.isRevoked());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_success() {
        String newAccessToken = "new-access-token";
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        newRefreshToken.setUser(testUser);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenAndRevokedFalse(testRefreshToken.getToken()))
                .thenReturn(Optional.of(testRefreshToken));
        when(jwtTokenProvider.generateToken(testUser)).thenReturn(newAccessToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshToken);
        when(refreshTokenRepository.countValidTokensByUser(any(User.class), any(LocalDateTime.class))).thenReturn(0L);

        AuthResponse result = refreshTokenService.refreshAccessToken(testRefreshToken.getToken());

        assertNotNull(result);
        assertEquals(newAccessToken, result.getToken());
        assertNotNull(result.getRefreshToken());
        verify(refreshTokenRepository).findByTokenAndRevokedFalse(testRefreshToken.getToken());
        verify(jwtTokenProvider).generateToken(testUser);
    }

    @Test
    void refreshAccessToken_tokenNotFound_throwsException() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class,
                () -> refreshTokenService.refreshAccessToken("invalid-token"));
    }

    @Test
    void refreshAccessToken_expiredToken_throwsException() {
        testRefreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByTokenAndRevokedFalse(testRefreshToken.getToken()))
                .thenReturn(Optional.of(testRefreshToken));
        doNothing().when(refreshTokenRepository).revokeByToken(testRefreshToken.getToken());

        assertThrows(TokenExpiredException.class,
                () -> refreshTokenService.refreshAccessToken(testRefreshToken.getToken()));
    }

    @Test
    void revokeRefreshToken_success() {
        doNothing().when(refreshTokenRepository).revokeByToken(testRefreshToken.getToken());

        refreshTokenService.revokeRefreshToken(testRefreshToken.getToken());

        verify(refreshTokenRepository).revokeByToken(testRefreshToken.getToken());
    }

    @Test
    void revokeAllUserTokens_success() {
        doNothing().when(refreshTokenRepository).revokeAllByUser(testUser);

        refreshTokenService.revokeAllUserTokens(testUser);

        verify(refreshTokenRepository).revokeAllByUser(testUser);
    }

    @Test
    void cleanupExpiredTokens_success() {
        doNothing().when(refreshTokenRepository).deleteExpiredAndRevokedTokens(any(LocalDateTime.class));

        refreshTokenService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteExpiredAndRevokedTokens(any(LocalDateTime.class));
    }
}