package com.donggle.domain.announce.controller;

import com.donggle.domain.announce.domain.Announce;
import com.donggle.domain.announce.dto.AnnounceRequest;
import com.donggle.domain.announce.dto.AnnounceResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지", description = "공지사항 관련 API")
public interface AnnounceApi {

    @Operation(summary = "일반 공지사항 생성", description = "새로운 일반 공지사항을 생성합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "공지사항 생성 성공",
                        content =
                                @Content(
                                        schema = @Schema(implementation = AnnounceResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음")
            })
    @PostMapping
    ResponseEntity<AnnounceResponse> createGeneralAnnounce(
            @Valid @RequestBody AnnounceRequest request,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "동아리 공지사항 생성", description = "새로운 동아리 공지사항을 생성합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "공지사항 생성 성공",
                        content =
                                @Content(
                                        schema = @Schema(implementation = AnnounceResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @PostMapping("/clubs/{clubId}")
    ResponseEntity<AnnounceResponse> createClubAnnounce(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Valid @RequestBody AnnounceRequest request,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다. 작성자 또는 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "공지사항 수정 성공",
                        content =
                                @Content(
                                        schema = @Schema(implementation = AnnounceResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
            })
    @PutMapping("/{announceId}")
    ResponseEntity<AnnounceResponse> updateAnnounce(
            @Parameter(description = "공지사항 ID") @PathVariable Long announceId,
            @Valid @RequestBody AnnounceRequest request,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. 작성자 또는 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "공지사항 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
            })
    @DeleteMapping("/{announceId}")
    ResponseEntity<Void> deleteAnnounce(
            @Parameter(description = "공지사항 ID") @PathVariable Long announceId,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "공지사항 조회", description = "공지사항 ID로 공지사항 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "공지사항 조회 성공",
                        content =
                                @Content(
                                        schema = @Schema(implementation = AnnounceResponse.class))),
                @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
            })
    @GetMapping("/{announceId}")
    ResponseEntity<AnnounceResponse> getAnnounce(
            @Parameter(description = "공지사항 ID") @PathVariable Long announceId);

    @Operation(summary = "타입별 공지사항 목록 조회", description = "공지사항 타입(일반, 동아리)별로 공지사항 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공")})
    @GetMapping("/type/{type}")
    ResponseEntity<Page<AnnounceResponse>> getAnnouncesByType(
            @Parameter(description = "공지사항 타입(GENERAL, CLUB)") @PathVariable
                    Announce.AnnounceType type,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable);

    @Operation(summary = "동아리별 공지사항 목록 조회", description = "특정 동아리의 공지사항 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @GetMapping("/clubs/{clubId}")
    ResponseEntity<Page<AnnounceResponse>> getAnnouncesByClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable);

    @Operation(summary = "타입별 최근 공지사항 조회", description = "공지사항 타입(일반, 동아리)별로 최근 공지사항을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공")})
    @GetMapping("/recent/type/{type}")
    ResponseEntity<List<AnnounceResponse>> getRecentAnnouncesByType(
            @Parameter(description = "공지사항 타입(GENERAL, CLUB)") @PathVariable
                    Announce.AnnounceType type);

    @Operation(summary = "동아리별 최근 공지사항 조회", description = "특정 동아리의 최근 공지사항을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @GetMapping("/recent/clubs/{clubId}")
    ResponseEntity<List<AnnounceResponse>> getRecentAnnouncesByClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId);
}
