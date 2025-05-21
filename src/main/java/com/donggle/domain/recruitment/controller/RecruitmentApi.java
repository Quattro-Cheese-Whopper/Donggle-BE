package com.donggle.domain.recruitment.controller;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.recruitment.domain.Recruitment;
import com.donggle.domain.recruitment.dto.RecruitmentRequest;
import com.donggle.domain.recruitment.dto.RecruitmentResponse;
import com.donggle.global.auth.resolver.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "모집", description = "모집 공고 관련 API")
public interface RecruitmentApi {

    @Operation(summary = "모집 공고 생성", description = "동아리별 모집 공고를 생성합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "모집 공고 생성 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                RecruitmentResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @PostMapping("/clubs/{clubId}")
    ResponseEntity<RecruitmentResponse> createRecruitment(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Valid @RequestBody RecruitmentRequest request,
            @UserId Long userId);

    @Operation(summary = "모집 공고 수정", description = "모집 공고를 수정합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "모집 공고 수정 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                RecruitmentResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @PutMapping("/{recruitmentId}")
    ResponseEntity<RecruitmentResponse> updateRecruitment(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @Valid @RequestBody RecruitmentRequest request,
            @UserId Long userId);

    @Operation(summary = "모집 공고 삭제", description = "모집 공고를 삭제합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "모집 공고 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @DeleteMapping("/{recruitmentId}")
    ResponseEntity<Void> deleteRecruitment(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @UserId Long userId);

    @Operation(summary = "모집 공고 조회", description = "모집 공고 ID로 모집 공고 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "모집 공고 조회 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                RecruitmentResponse.class))),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @GetMapping("/{recruitmentId}")
    ResponseEntity<RecruitmentResponse> getRecruitment(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId);

    @Operation(summary = "전체 모집 공고 목록 조회", description = "모든 모집 공고 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공")})
    @GetMapping
    ResponseEntity<List<RecruitmentResponse>> getAllRecruitments();

    @Operation(summary = "진행 중인 모집 공고 목록 조회", description = "현재 진행 중인 모집 공고 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공")})
    @GetMapping("/active")
    ResponseEntity<List<RecruitmentResponse>> getActiveRecruitments();

    @Operation(summary = "동아리별 모집 공고 목록 조회", description = "특정 동아리의 모집 공고 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @GetMapping("/clubs/{clubId}")
    ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId);

    @Operation(summary = "상태별 모집 공고 목록 조회", description = "모집 상태(준비중, 모집중, 마감)별로 모집 공고 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공")})
    @GetMapping("/status/{status}")
    ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByStatus(
            @Parameter(description = "모집 상태(PREPARING, RECRUITING, CLOSED)") @PathVariable
                    Recruitment.RecruitmentStatus status);

    @Operation(
            summary = "동아리 타입 및 상태별 모집 공고 목록 조회",
            description = "동아리 타입(중앙동아리, 학과동아리)과 모집 상태로 필터링하여 모집 공고 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공")})
    @GetMapping("/club-type/{type}/status/{status}")
    ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClubType(
            @Parameter(description = "동아리 타입(CENTRAL, DEPARTMENT)") @PathVariable
                    Club.ClubType type,
            @Parameter(description = "모집 상태(PREPARING, RECRUITING, CLOSED)") @PathVariable
                    Recruitment.RecruitmentStatus status);

    @Operation(
            summary = "동아리 카테고리 및 상태별 모집 공고 목록 조회",
            description = "동아리 카테고리(학술, 문화, 체육 등)와 모집 상태로 필터링하여 모집 공고 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모집 공고 목록 조회 성공")})
    @GetMapping("/club-category/{category}/status/{status}")
    ResponseEntity<List<RecruitmentResponse>> getRecruitmentsByClubCategory(
            @Parameter(description = "동아리 카테고리") @PathVariable Club.ClubCategory category,
            @Parameter(description = "모집 상태(PREPARING, RECRUITING, CLOSED)") @PathVariable
                    Recruitment.RecruitmentStatus status);

    @Operation(summary = "모집 공고 상태 변경", description = "모집 공고의 상태를 변경합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "모집 공고 상태 변경 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @PatchMapping("/{recruitmentId}/status/{status}")
    ResponseEntity<Void> updateRecruitmentStatus(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "변경할 상태(PREPARING, RECRUITING, CLOSED)") @PathVariable
                    Recruitment.RecruitmentStatus status,
            @UserId Long userId);
}
