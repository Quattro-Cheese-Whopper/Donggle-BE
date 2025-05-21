package com.donggle.domain.file.dto;

import com.donggle.domain.file.domain.FileEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private Long id;
    private String originalName;
    private String extension;
    private String contentType;
    private Long size;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static FileResponse from(FileEntity file, String baseUrl) {
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getExtension(),
                file.getContentType(),
                file.getSize(),
                baseUrl + "/api/files/" + file.getStoredName(),
                file.getCreatedAt());
    }
}
