package com.example.meanhwa_back.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.example.meanhwa_back.user.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

/** Access/Refresh JWT 발급·검증 및 클레임 추출. */
@Component
public class JwtTokenProvider {
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "ACCESS";
    private static final String REFRESH_TYPE = "REFRESH";
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
/**
 * 사용자 식별자와 권한을 담은 access token을 발급한다.
 */

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtProperties.getAccessTokenValidityMinutes() * 60);
        return createToken(user, ACCESS_TYPE, now, expiration);
    }
/**
 * 사용자 식별자를 담은 refresh token을 발급한다.
 */

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtProperties.getRefreshTokenValidityDays() * 24 * 60 * 60);
        return createToken(user, REFRESH_TYPE, now, expiration);
    }

    public Long getUserIdFromAccessToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, ACCESS_TYPE);
        return Long.valueOf(claims.getSubject());
    }

    public long getAccessTokenExpiresInSeconds() {
        return jwtProperties.getAccessTokenValidityMinutes() * 60;
    }

    public LocalDateTime getRefreshTokenExpiresAt(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TYPE);
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZONE_ID);
    }
/**
 * refresh token 서명, 만료, token type claim을 검증한다.
 */

    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TYPE);
    }

    private String createToken(User user, String tokenType, Instant issuedAt, Instant expiration) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateTokenType(Claims claims, String expectedType) {
        if (!expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }
    }
}
