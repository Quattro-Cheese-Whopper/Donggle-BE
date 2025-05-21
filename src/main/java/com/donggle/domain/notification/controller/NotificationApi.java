package com.donggle.domain.notification.controller;

import com.donggle.domain.notification.dto.NotificationResponse;
import com.donggle.global.auth.resolver.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping
    ResponseEntity<Page<NotificationResponse>> getNotifications(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable, @UserId Long userId);

    @Operation(summary = "미읽은 알림 목록 조회", description = "사용자의 미읽은 알림 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "미읽은 알림 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/unread")
    ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable, @UserId Long userId);

    @Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "알림 조회 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                NotificationResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
            })
    @GetMapping("/{notificationId}")
    ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @UserId Long userId);

    @Operation(summary = "알림 읽음 표시", description = "특정 알림을 읽음 표시합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "읽음 표시 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                NotificationResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
            })
    @PatchMapping("/{notificationId}/read")
    ResponseEntity<NotificationResponse> markAsRead(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @UserId Long userId);

    @Operation(summary = "모든 알림 읽음 표시", description = "사용자의 모든 알림을 읽음 표시합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "모든 알림 읽음 표시 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @PatchMapping("/read-all")
    ResponseEntity<Void> markAllAsRead(@UserId Long userId);

    @Operation(summary = "미읽은 알림 수 조회", description = "사용자의 미읽은 알림 수를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "미읽은 알림 수 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/unread/count")
    ResponseEntity<Long> countUnreadNotifications(@UserId Long userId);

    @Operation(summary = "최근 미읽은 알림 조회", description = "사용자의 최근 미읽은 알림 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "최근 미읽은 알림 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/recent")
    ResponseEntity<List<NotificationResponse>> getRecentUnreadNotifications(@UserId Long userId);
}
