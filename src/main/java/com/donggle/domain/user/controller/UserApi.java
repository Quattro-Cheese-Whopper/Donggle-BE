package com.donggle.domain.user.controller;

import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.dto.UserProfileResponse;
import com.donggle.domain.user.dto.UserUpdateRequest;
import com.donggle.global.auth.resolver.UserId;
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

@Tag(name = "회원", description = "회원 관련 API")
public interface UserApi {

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "프로필 조회 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                UserProfileResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            })
    @GetMapping("/me")
    ResponseEntity<UserProfileResponse> getCurrentUser(@UserId Long userId);

    @Operation(summary = "회원 정보 조회", description = "특정 회원의 프로필 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "프로필 조회 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                UserProfileResponse.class))),
                @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            })
    @GetMapping("/{userId}")
    ResponseEntity<UserProfileResponse> getUserById(
            @Parameter(description = "사용자 ID") @PathVariable Long userId);

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "프로필 수정 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                UserProfileResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            })
    @PutMapping("/me")
    ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest request, @UserId Long userId);

    @Operation(summary = "회원 권한 변경", description = "특정 회원의 권한을 변경합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 변경 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            })
    @PatchMapping("/{userId}/role")
    ResponseEntity<Void> updateUserRole(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "변경할 권한") @RequestParam User.UserRole role,
            @UserId Long adminId);
}
