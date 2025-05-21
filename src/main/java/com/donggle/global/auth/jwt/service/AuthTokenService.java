package com.donggle.global.auth.jwt.service;

import com.donggle.global.auth.jwt.AuthToken;
import com.donggle.global.auth.jwt.repository.TokenRepository;
import com.donggle.global.error.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String GRANT_TYPE = "Bearer";

    @Value("${jwt.access-token-expiration-minutes}")
    private Long accessTokenExpireTime;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @Transactional
    public AuthToken generateAuthToken(Long userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        return AuthToken.of(accessToken, refreshToken, GRANT_TYPE, accessTokenExpireTime * 60);
    }

    @Transactional
    public AuthToken reissue(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        if (!tokenRepository.existsByToken(refreshToken)) {
            throw InvalidRefreshTokenException.EXCEPTION;
        }

        return generateAuthToken(userId);
    }

    @Transactional
    public void logout(Long userId) {
        tokenRepository.deleteById(userId);
    }
}
