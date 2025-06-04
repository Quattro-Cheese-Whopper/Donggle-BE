package com.donggle.domain.file.controller;

import com.donggle.domain.file.domain.FileEntity;
import com.donggle.domain.file.dto.FileResponse;
import com.donggle.domain.file.service.FileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileService fileService;

    @Override
    public ResponseEntity<FileResponse> uploadFile(
            FileEntity.FileType fileType, Long relatedId, MultipartFile file) throws IOException {

        FileResponse response = fileService.uploadFile(file, fileType, relatedId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<FileResponse> getFileInfo(Long fileId) {
        FileResponse response = fileService.getFile(fileId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<FileResponse>> getFilesByTypeAndRelatedId(
            FileEntity.FileType fileType, Long relatedId) {

        List<FileResponse> response = fileService.getFilesByTypeAndRelatedId(fileType, relatedId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String storedName) throws IOException {
        FileEntity file = fileService.findByStoredName(storedName);

        Path path = Paths.get(file.getFilePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        log.info("Downloading file: {}", file.getOriginalName());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getSize())
                .body(resource);
    }

    @Override
    public ResponseEntity<Void> deleteFile(Long fileId, Long userId) throws IOException {

        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteFilesByTypeAndRelatedId(
            FileEntity.FileType fileType, Long relatedId, Long userId) throws IOException {

        fileService.deleteFilesByTypeAndRelatedId(fileType, relatedId);
        return ResponseEntity.noContent().build();
    }
}
