package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.AuthResponse;
import com.mafia.exceptions.TokenExpiredException;
import com.mafia.exceptions.TokenNotFoundException;
import com.mafia.models.RefreshToken;
import com.mafia.models.User;
import com.mafia.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration:604800000}") // 7 dni w milisekundach
    private long refreshTokenExpiration;

    @Value("${jwt.max-refresh-tokens-per-user:5}")
    private int maxRefreshTokensPerUser;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceInfo) {
        // Sprawdź limit tokenów na użytkownika
        cleanupUserTokensIfNeeded(user);

        // Generuj bezpieczny token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = new RefreshToken(token, user, expiresAt);
        refreshToken.setDeviceInfo(deviceInfo);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenString)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found or revoked"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.revokeByToken(refreshTokenString);
            throw new TokenExpiredException("Refresh token has expired");
        }

        // Generuj nowy access token
        String newAccessToken = jwtTokenProvider.generateToken(refreshToken.getUser());

        // Opcjonalnie: rotacja refresh tokena (generuj nowy refresh token)
        RefreshToken newRefreshToken = rotateRefreshToken(refreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    protected RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Unieważnij stary token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Stwórz nowy token
        return createRefreshToken(oldToken.getUser(), oldToken.getDeviceInfo());
    }

    private void cleanupUserTokensIfNeeded(User user) {
        long validTokensCount = refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());

        if (validTokensCount >= maxRefreshTokensPerUser) {
            // Usuń najstarsze tokeny użytkownika
            List<RefreshToken> userTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
            userTokens.stream()
                    .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .limit(userTokens.size() - maxRefreshTokensPerUser + 1)
                    .forEach(token -> token.setRevoked(true));

            refreshTokenRepository.saveAll(userTokens);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Czyszczenie wygasłych tokenów - uruchamiane co godzinę
    @Scheduled(fixedRate = 3600000) // 1 godzina
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
    }
}