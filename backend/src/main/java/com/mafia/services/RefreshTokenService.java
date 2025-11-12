package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.databaseModels.RefreshToken;
import com.mafia.databaseModels.User;
import com.mafia.dto.auth.AuthResp;
import com.mafia.exceptions.TokenExpiredException;
import com.mafia.exceptions.TokenNotFoundException;
import com.mafia.repositories.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${jwt.refresh-expiration:604800000}")
  private long refreshTokenExpiration;

  @Value("${jwt.max-refresh-tokens-per-user:5}")
  private int maxRefreshTokensPerUser;

  private final SecureRandom secureRandom = new SecureRandom();

  @Transactional
  public RefreshToken createRefreshToken(User user, String deviceInfo) {

    cleanupUserTokensIfNeeded(user);

    String token = generateSecureToken();
    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

    RefreshToken refreshToken = new RefreshToken(token, user, expiresAt);
    refreshToken.setDeviceInfo(deviceInfo);

    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional
  public AuthResp refreshAccessToken(String refreshTokenString) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenAndRevokedFalse(refreshTokenString)
            .orElseThrow(() -> new TokenNotFoundException("Refresh token not found or revoked"));

    if (refreshToken.isExpired()) {
      refreshTokenRepository.revokeByToken(refreshTokenString);
      throw new TokenExpiredException("Refresh token has expired");
    }

    String newAccessToken = jwtTokenProvider.generateToken(refreshToken.getUser());

    RefreshToken newRefreshToken = rotateRefreshToken(refreshToken);

    long expiresInSeconds = refreshTokenExpiration / 1000;
    return new AuthResp(newAccessToken, newRefreshToken.getToken(), "Bearer", expiresInSeconds);
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

    oldToken.setRevoked(true);
    refreshTokenRepository.save(oldToken);

    return createRefreshToken(oldToken.getUser(), oldToken.getDeviceInfo());
  }

  private void cleanupUserTokensIfNeeded(User user) {
    long validTokensCount =
        refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());

    if (validTokensCount >= maxRefreshTokensPerUser) {
      List<RefreshToken> userTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
      userTokens.stream()
          .sorted(
              Comparator.comparing(
                  RefreshToken::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
          .limit(userTokens.size() - (long) maxRefreshTokensPerUser + 1)
          .forEach(token -> token.setRevoked(true));

      refreshTokenRepository.saveAll(userTokens);
    }
  }

  private String generateSecureToken() {
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  @Scheduled(fixedRate = 3600000)
  @Transactional
  public void cleanupExpiredTokens() {
    refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
  }
}
