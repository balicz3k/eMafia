package com.mafia.components;

import com.mafia.models.Role;
import com.mafia.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Claims claims = Jwts.claims().setSubject(user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream().map(Role::name).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return UUID.fromString(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();

        UUID userId = UUID.fromString(claims.getSubject());
        String username = claims.get("username", String.class);

        @SuppressWarnings("unchecked")
        List<String> rolesStrings = claims.get("roles", List.class);
        List<GrantedAuthority> authorities;
        if (rolesStrings != null) {
            authorities = rolesStrings.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        } else {
            authorities = Collections.emptyList();
        }

        User principalUser = new User();
        principalUser.setId(userId);
        principalUser.setUsername(username);

        return new UsernamePasswordAuthenticationToken(principalUser, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public long getExpirationTime() {
        return expiration / 1000; // Zwracamy w sekundach (nie milisekundach)
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}