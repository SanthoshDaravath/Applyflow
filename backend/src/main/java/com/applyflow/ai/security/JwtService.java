package com.applyflow.ai.security;

import com.applyflow.ai.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secret;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
            @Value("${app.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
        this.secret = secret;
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    public String generateAccessToken(UserEntity user) {
        return buildToken(user.getEmail(), user.getId(), Map.of("role", user.getRole().name()), Instant.now().plus(accessTokenTtlMinutes, ChronoUnit.MINUTES));
    }

    public String generateRefreshToken(UserEntity user) {
        return buildToken(user.getEmail(), user.getId(), Map.of("tokenType", "refresh"), Instant.now().plus(refreshTokenTtlDays, ChronoUnit.DAYS));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equalsIgnoreCase(userDetails.getUsername()) && !isExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String value = extractClaims(token).get("uid", String.class);
        return value == null ? null : UUID.fromString(value);
    }

    public boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public long getAccessTokenTtlMinutes() {
        return accessTokenTtlMinutes;
    }

    public long getRefreshTokenTtlDays() {
        return refreshTokenTtlDays;
    }

    private String buildToken(String subject, UUID userId, Map<String, Object> claims, Instant expiry) {
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .claim("uid", userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key signingKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
