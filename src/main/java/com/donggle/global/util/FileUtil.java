package com.donggle.global.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtil {

    private static final String UPLOAD_DIR = "uploads";

    /**
     * 파일을 업로드 디렉토리에 저장합니다.
     *
     * @param file 업로드할 파일
     * @return 저장된 파일 경로 및 정보
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public FileInfo saveFile(MultipartFile file) throws IOException {
        // 디렉토리 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일명 및 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        // 저장할 파일명 생성 (UUID + 확장자)
        String storedFilename = UUID.randomUUID() + "." + fileExtension;

        // 파일 저장
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return new FileInfo(
                originalFilename,
                storedFilename,
                fileExtension,
                file.getContentType(),
                file.getSize(),
                filePath.toString());
    }

    /**
     * 저장된 파일을 삭제합니다.
     *
     * @param filePath 삭제할 파일 경로
     * @throws IOException 파일 삭제 중 오류 발생 시
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }

    /** 파일 정보를 저장하기 위한 내부 클래스 */
    public record FileInfo(
            String originalName,
            String storedName,
            String extension,
            String contentType,
            long size,
            String filePath) {}
}
