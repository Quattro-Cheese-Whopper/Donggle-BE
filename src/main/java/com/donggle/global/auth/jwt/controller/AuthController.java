package com.donggle.global.auth.jwt.controller;

import com.donggle.domain.user.dto.TokenResponse;
import com.donggle.domain.user.dto.UserLoginRequest;
import com.donggle.domain.user.dto.UserSignupRequest;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.auth.controller.AuthApi;
import com.donggle.global.auth.jwt.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final AuthService authService;

    @Override
    public ResponseEntity<Void> signup(UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<TokenResponse> login(UserLoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Override
    public ResponseEntity<TokenResponse> refresh(String refreshToken) {
        TokenResponse tokenResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }

    @Override
    public ResponseEntity<Void> logout(Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }
}
