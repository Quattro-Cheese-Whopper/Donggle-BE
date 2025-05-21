package com.donggle.global.auth.jwt.service;

import com.donggle.global.auth.jwt.TokenType;
import com.donggle.global.error.exception.InvalidAccessTokenException;
import com.donggle.global.error.exception.InvalidRefreshTokenException;
import com.donggle.global.error.exception.MissingTokenException;
import com.donggle.global.error.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;
    private final Long accessTokenExpireTime;
    private final Long refreshTokenExpireTime;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-minutes}") Long accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token-expiration-minutes}") Long refreshTokenExpirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireTime = accessTokenExpirationMinutes * 60;
        this.refreshTokenExpireTime = refreshTokenExpirationMinutes * 60;
    }

    public String generateAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .claim("id", userId)
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime()
                                        + Duration.ofSeconds(accessTokenExpireTime).toMillis()))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .claim("id", userId)
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime()
                                        + Duration.ofSeconds(refreshTokenExpireTime).toMillis()))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserIdFromAccessToken(String accessToken) {
        Claims claims = parseClaims(accessToken, TokenType.ACCESS);
        return claims.get("id", Long.class);
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken, TokenType.REFRESH);
        return claims.get("id", Long.class);
    }

    public String extractToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw MissingTokenException.EXCEPTION;
        }
        return header.substring(BEARER_PREFIX.length());
    }

    public boolean validateRefreshToken(String token) {
        parseClaims(token, TokenType.REFRESH);
        return true;
    }

    private Claims parseClaims(String token, TokenType tokenType) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token is expired");
        } catch (Exception e) {
            if (tokenType.equals(TokenType.ACCESS)) {
                throw InvalidAccessTokenException.EXCEPTION;
            }
            throw InvalidRefreshTokenException.EXCEPTION;
        }
    }

    public boolean existsByMemberId(Long memberId) {

        return true;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public long getAccessTokenExpirationMillis() {
        return accessTokenExpireTime * 1000;
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpireTime * 1000;
    }
}
