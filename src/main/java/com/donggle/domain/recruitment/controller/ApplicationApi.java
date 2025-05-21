package com.donggle.domain.recruitment.controller;

import com.donggle.domain.recruitment.domain.Application;
import com.donggle.domain.recruitment.dto.ApplicationRequest;
import com.donggle.domain.recruitment.dto.ApplicationResponse;
import com.donggle.domain.recruitment.dto.ApplicationStatusRequest;
import com.donggle.global.auth.resolver.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지원", description = "지원서 관련 API")
public interface ApplicationApi {

    @Operation(summary = "지원서 제출", description = "모집 공고에 지원서를 제출합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "지원서 제출 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ApplicationResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음"),
                @ApiResponse(responseCode = "409", description = "이미 지원한 모집 공고")
            })
    @PostMapping("/recruitments/{recruitmentId}")
    ResponseEntity<ApplicationResponse> createApplication(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @Valid @RequestBody ApplicationRequest request,
            @UserId Long userId);

    @Operation(summary = "지원서 수정", description = "제출한 지원서를 수정합니다. 지원 상태가 대기 중인 경우에만 가능합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "지원서 수정 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ApplicationResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음"),
                @ApiResponse(responseCode = "409", description = "수정할 수 없는 상태")
            })
    @PutMapping("/{applicationId}")
    ResponseEntity<ApplicationResponse> updateApplication(
            @Parameter(description = "지원서 ID") @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationRequest request,
            @UserId Long userId);

    @Operation(summary = "지원 취소", description = "제출한 지원을 취소합니다. 지원 상태가 대기 중인 경우에만 가능합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "지원 취소 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음"),
                @ApiResponse(responseCode = "409", description = "취소할 수 없는 상태")
            })
    @DeleteMapping("/{applicationId}")
    ResponseEntity<Void> cancelApplication(
            @Parameter(description = "지원서 ID") @PathVariable Long applicationId,
            @UserId Long userId);

    @Operation(
            summary = "지원 상태 변경",
            description = "지원 상태를 변경합니다(합격, 불합격, 대기 등). 동아리 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "지원 상태 변경 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ApplicationResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음")
            })
    @PatchMapping("/{applicationId}/status")
    ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @Parameter(description = "지원서 ID") @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusRequest request,
            @UserId Long userId);

    @Operation(
            summary = "지원서 조회",
            description = "지원서 상세 정보를 조회합니다. 지원자 본인 또는 해당 동아리 관리자만 조회 가능합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "지원서 조회 성공",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                ApplicationResponse.class))),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음")
            })
    @GetMapping("/{applicationId}")
    ResponseEntity<ApplicationResponse> getApplication(
            @Parameter(description = "지원서 ID") @PathVariable Long applicationId,
            @UserId Long userId);

    @Operation(
            summary = "모집별 지원서 목록 조회",
            description = "특정 모집 공고에 대한 지원서 목록을 조회합니다. 해당 동아리 관리자만 조회 가능합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @GetMapping("/recruitments/{recruitmentId}")
    ResponseEntity<Page<ApplicationResponse>> getApplicationsByRecruitment(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @PageableDefault(size = 10) Pageable pageable,
            @UserId Long userId);

    @Operation(
            summary = "동아리별 지원서 목록 조회",
            description = "특정 동아리에 대한 지원서 목록을 조회합니다. 해당 동아리 관리자만 조회 가능합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @GetMapping("/clubs/{clubId}")
    ResponseEntity<Page<ApplicationResponse>> getApplicationsByClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @PageableDefault(size = 10) Pageable pageable,
            @UserId Long userId);

    @Operation(summary = "내 지원서 목록 조회", description = "로그인한 사용자가 제출한 지원서 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/me")
    ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @PageableDefault(size = 10) Pageable pageable, @UserId Long userId);

    @Operation(summary = "상태별 내 지원서 목록 조회", description = "상태(대기, 합격, 불합격 등)별로 내 지원서 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/me/status/{status}")
    ResponseEntity<Page<ApplicationResponse>> getMyApplicationsByStatus(
            @Parameter(description = "지원 상태(PENDING, ACCEPTED, REJECTED, CANCELED)") @PathVariable
                    Application.ApplicationStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            @UserId Long userId);

    @Operation(summary = "모집별 상태별 지원 수 집계", description = "특정 모집 공고에 대한 상태별 지원 수를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "집계 조회 성공"),
                @ApiResponse(responseCode = "404", description = "모집 공고를 찾을 수 없음")
            })
    @GetMapping("/recruitments/{recruitmentId}/count/{status}")
    ResponseEntity<Long> countApplicationsByRecruitmentAndStatus(
            @Parameter(description = "모집 공고 ID") @PathVariable Long recruitmentId,
            @Parameter(description = "지원 상태(PENDING, ACCEPTED, REJECTED, CANCELED)") @PathVariable
                    Application.ApplicationStatus status);
}
