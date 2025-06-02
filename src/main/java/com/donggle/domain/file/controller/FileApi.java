package com.donggle.domain.file.controller;

import com.donggle.domain.file.domain.FileEntity;
import com.donggle.domain.file.dto.FileResponse;
import com.donggle.global.auth.resolver.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "파일", description = "파일 관련 API")
public interface FileApi {

    @Operation(summary = "파일 업로드", description = "파일을 업로드합니다. 파일 타입과 관련 ID를 지정해야 합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "파일 업로드 성공",
                        content = @Content(schema = @Schema(implementation = FileResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    @PostMapping(
            value = "/upload/{fileType}/{relatedId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileResponse> uploadFile(
            @Parameter(
                            description =
                                    "파일 타입(CLUB_PROFILE, CLUB_BANNER, RECRUITMENT_ATTACHMENT, NOTICE_ATTACHMENT 등)")
                    @PathVariable
                    FileEntity.FileType fileType,
            @Parameter(description = "관련 엔티티 ID(동아리 ID, 모집 공고 ID, 공지사항 ID 등)") @PathVariable
                    Long relatedId,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file)
            throws IOException;

    @Operation(summary = "파일 정보 조회", description = "파일 ID로 파일 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파일 정보 조회 성공",
                        content = @Content(schema = @Schema(implementation = FileResponse.class))),
                @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
            })
    @GetMapping("/{fileId}")
    ResponseEntity<FileResponse> getFileInfo(
            @Parameter(description = "파일 ID") @PathVariable Long fileId);

    @Operation(summary = "타입 및 관련 ID별 파일 목록 조회", description = "특정 타입과 관련 ID에 속하는 파일 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "파일 목록 조회 성공")})
    @GetMapping("/{fileType}/{relatedId}")
    ResponseEntity<List<FileResponse>> getFilesByTypeAndRelatedId(
            @Parameter(
                            description =
                                    "파일 타입(CLUB_PROFILE, CLUB_BANNER, RECRUITMENT_ATTACHMENT, NOTICE_ATTACHMENT 등)")
                    @PathVariable
                    FileEntity.FileType fileType,
            @Parameter(description = "관련 엔티티 ID(동아리 ID, 모집 공고 ID, 공지사항 ID 등)") @PathVariable
                    Long relatedId);

    @Operation(summary = "파일 다운로드", description = "저장된 파일명으로 파일을 다운로드합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파일 다운로드 성공",
                        content = @Content(mediaType = "application/octet-stream")),
                @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    @GetMapping("/download/{storedName}")
    ResponseEntity<Resource> downloadFile(
            @Parameter(description = "서버에 저장된 파일명") @PathVariable String storedName)
            throws IOException;

    @Operation(summary = "파일 삭제", description = "파일 ID로 파일을 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "파일 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    @DeleteMapping("/{fileId}")
    ResponseEntity<Void> deleteFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @Parameter(hidden = true) @UserId Long userId)
            throws IOException;

    @Operation(summary = "타입 및 관련 ID별 파일 일괄 삭제", description = "특정 타입과 관련 ID에 속하는 모든 파일을 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "파일 삭제 성공"),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    @DeleteMapping("/{fileType}/{relatedId}")
    ResponseEntity<Void> deleteFilesByTypeAndRelatedId(
            @Parameter(
                            description =
                                    "파일 타입(CLUB_PROFILE, CLUB_BANNER, RECRUITMENT_ATTACHMENT, NOTICE_ATTACHMENT 등)")
                    @PathVariable
                    FileEntity.FileType fileType,
            @Parameter(description = "관련 엔티티 ID(동아리 ID, 모집 공고 ID, 공지사항 ID 등)") @PathVariable
                    Long relatedId,
            @Parameter(hidden = true) @UserId Long userId)
            throws IOException;
}
