package com.donggle.global.auth.controller;

import com.donggle.domain.user.dto.TokenResponse;
import com.donggle.domain.user.dto.UserLoginRequest;
import com.donggle.domain.user.dto.UserSignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "인증 관련 API")
public interface AuthApi {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 이미 존재하는 사용자")
            })
    @PostMapping("/signup")
    ResponseEntity<Void> signup(@Valid @RequestBody UserSignupRequest request);

    @Operation(summary = "로그인", description = "사용자 인증 후 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "로그인 성공",
                        content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody UserLoginRequest request);

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "토큰 갱신 성공",
                        content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
            })
    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refresh(
            @Parameter(description = "리프레시 토큰", required = true) @RequestHeader("Refresh-Token")
                    String refreshToken);

    @Operation(summary = "로그아웃", description = "사용자의 토큰을 무효화합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@Parameter(hidden = true) @RequestAttribute("userId") Long userId);
}
