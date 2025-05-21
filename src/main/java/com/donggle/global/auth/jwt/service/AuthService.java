package com.donggle.global.auth.jwt.service;

import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.dto.TokenResponse;
import com.donggle.domain.user.dto.UserLoginRequest;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.auth.jwt.AuthToken;
import com.donggle.global.auth.jwt.RefreshToken;
import com.donggle.global.auth.jwt.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(UserLoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        AuthToken authToken = authTokenService.generateAuthToken(user.getId());

        // Redis에 RefreshToken 저장
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .id(user.getId())
                        .token(authToken.refreshToken())
                        .expiration(jwtTokenProvider.getRefreshTokenExpirationMillis() / 1000)
                        .build();
        tokenRepository.save(refreshToken);

        return TokenResponse.of(authToken.accessToken(), authToken.refreshToken());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        AuthToken authToken = authTokenService.reissue(refreshToken);

        // 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        // Redis에 새 RefreshToken 저장
        RefreshToken newRefreshToken =
                RefreshToken.builder()
                        .id(userId)
                        .token(authToken.refreshToken())
                        .expiration(jwtTokenProvider.getRefreshTokenExpirationMillis() / 1000)
                        .build();
        tokenRepository.save(newRefreshToken);

        return TokenResponse.of(authToken.accessToken(), authToken.refreshToken());
    }

    @Transactional
    public void logout(Long userId) {
        authTokenService.logout(userId);
    }
}
