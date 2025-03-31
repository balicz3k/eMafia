// filepath: /Users/balicz3k/Documents/Mafia/backend/src/main/java/com/mafia/components/JwtTokenProvider.java
package com.mafia.components;

import com.mafia.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component public class JwtTokenProvider
{

    @Value("${jwt.secret}") private String secret;

    @Value("${jwt.expiration}") private long expiration;

    private Key getSigningKey()
    {
        // Dekodowanie klucza Base64 i tworzenie obiektu Key
        return new SecretKeySpec(Base64.getDecoder().decode(secret), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(User user)
    {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // Użycie nowej metody signWith z obiektem Key
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("username", user.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public UUID getUserIdFromToken(String token)
    {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token)
    {
        try
        {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }
        catch (Exception ex)
        {
            // Logowanie błędów
            return false;
        }
    }
}