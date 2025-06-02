package com.donggle.domain.club.controller;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.dto.ClubRequest;
import com.donggle.domain.club.dto.ClubResponse;
import com.donggle.global.annotation.AllowAnonymous;
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

@Tag(name = "동아리", description = "동아리 관련 API")
public interface ClubApi {

    @Operation(summary = "동아리 생성", description = "새로운 동아리를 생성합니다. 관리자 권한 또는 어드민 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "동아리 생성 성공",
                        content = @Content(schema = @Schema(implementation = ClubResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음")
            })
    @PostMapping
    ResponseEntity<ClubResponse> createClub(
            @Valid @RequestBody ClubRequest request, @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "동아리 수정", description = "동아리 정보를 수정합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "동아리 수정 성공",
                        content = @Content(schema = @Schema(implementation = ClubResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @PutMapping("/{clubId}")
    ResponseEntity<ClubResponse> updateClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Valid @RequestBody ClubRequest request,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "동아리 삭제", description = "동아리를 삭제합니다. 해당 동아리의 관리자 권한 또는 어드민 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "동아리 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @DeleteMapping("/{clubId}")
    ResponseEntity<Void> deleteClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Parameter(hidden = true) @UserId Long userId);

    @AllowAnonymous
    @Operation(summary = "동아리 조회", description = "동아리 ID로 동아리 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "동아리 조회 성공",
                        content = @Content(schema = @Schema(implementation = ClubResponse.class))),
                @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
            })
    @GetMapping("/{clubId}")
    ResponseEntity<ClubResponse> getClub(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId);

    @AllowAnonymous
    @Operation(summary = "전체 동아리 목록 조회", description = "모든 동아리 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "동아리 목록 조회 성공")})
    @GetMapping
    ResponseEntity<List<ClubResponse>> getAllClubs();

    @AllowAnonymous
    @Operation(summary = "동아리 타입별 조회", description = "동아리 타입(중앙동아리, 학과동아리)별로 동아리 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "동아리 목록 조회 성공")})
    @GetMapping("/type/{type}")
    ResponseEntity<List<ClubResponse>> getClubsByType(
            @Parameter(description = "동아리 타입(CENTRAL, DEPARTMENT)") @PathVariable
                    Club.ClubType type);

    @AllowAnonymous
    @Operation(summary = "동아리 카테고리별 조회", description = "동아리 카테고리(학술, 문화, 체육 등)별로 동아리 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "동아리 목록 조회 성공")})
    @GetMapping("/category/{category}")
    ResponseEntity<List<ClubResponse>> getClubsByCategory(
            @Parameter(description = "동아리 카테고리") @PathVariable Club.ClubCategory category);

    @AllowAnonymous
    @Operation(summary = "동아리 필터링 조회", description = "동아리 타입과 카테고리로 필터링하여 동아리 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "동아리 목록 조회 성공")})
    @GetMapping("/filter")
    ResponseEntity<Page<ClubResponse>> getClubsByTypeAndCategory(
            @Parameter(description = "동아리 타입(CENTRAL, DEPARTMENT)") @RequestParam
                    Club.ClubType type,
            @Parameter(description = "동아리 카테고리") @RequestParam Club.ClubCategory category,
            @PageableDefault(size = 10, sort = "name") Pageable pageable);

    @AllowAnonymous
    @Operation(summary = "동아리 검색", description = "키워드로 동아리를 검색합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "동아리 검색 성공")})
    @GetMapping("/search")
    ResponseEntity<Page<ClubResponse>> searchClubs(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "name") Pageable pageable);

    @Operation(summary = "내가 관리하는 동아리 목록 조회", description = "로그인한 사용자가 관리하는 동아리 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "동아리 목록 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/my-clubs")
    ResponseEntity<List<ClubResponse>> getMyClubs(@Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "동아리 관리자 추가", description = "동아리에 관리자를 추가합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "관리자 추가 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리 또는 사용자를 찾을 수 없음")
            })
    @PostMapping("/{clubId}/managers/{managerId}")
    ResponseEntity<Void> addManager(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Parameter(description = "추가할 관리자 ID") @PathVariable Long managerId,
            @Parameter(hidden = true) @UserId Long userId);

    @Operation(summary = "동아리 관리자 제거", description = "동아리에서 관리자를 제거합니다. 해당 동아리의 관리자 권한이 필요합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "관리자 제거 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "동아리 또는 사용자를 찾을 수 없음")
            })
    @DeleteMapping("/{clubId}/managers/{managerId}")
    ResponseEntity<Void> removeManager(
            @Parameter(description = "동아리 ID") @PathVariable Long clubId,
            @Parameter(description = "제거할 관리자 ID") @PathVariable Long managerId,
            @Parameter(hidden = true) @UserId Long userId);
}
