package com.donggle.domain.file.service;

import com.donggle.domain.file.domain.FileEntity;
import com.donggle.domain.file.dto.FileResponse;
import com.donggle.domain.file.repository.FileRepository;
import com.donggle.global.error.exception.EntityNotFoundException;
import com.donggle.global.util.FileUtil;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final String BASE_URL = "http://localhost:8080"; // 환경 변수로 관리하는 것이 좋음

    private final FileRepository fileRepository;
    private final FileUtil fileUtil;

    @Transactional
    public FileResponse uploadFile(MultipartFile file, FileEntity.FileType fileType, Long relatedId)
            throws IOException {
        // 파일 저장
        FileUtil.FileInfo fileInfo = fileUtil.saveFile(file);

        // DB에 파일 정보 저장
        FileEntity fileEntity =
                new FileEntity(
                        fileInfo.getOriginalName(),
                        fileInfo.getStoredName(),
                        fileInfo.getExtension(),
                        fileInfo.getContentType(),
                        fileInfo.getSize(),
                        fileInfo.getFilePath(),
                        fileType,
                        relatedId);

        FileEntity savedFile = fileRepository.save(fileEntity);

        return FileResponse.from(savedFile, BASE_URL);
    }

    @Transactional(readOnly = true)
    public FileEntity findById(Long id) {
        return fileRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(Long fileId) {
        FileEntity file = findById(fileId);
        return FileResponse.from(file, BASE_URL);
    }

    @Transactional(readOnly = true)
    public FileEntity findByStoredName(String storedName) {
        return fileRepository
                .findByStoredName(storedName)
                .orElseThrow(
                        () -> new EntityNotFoundException("파일을 찾을 수 없습니다. 파일명: " + storedName));
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFilesByTypeAndRelatedId(
            FileEntity.FileType fileType, Long relatedId) {
        return fileRepository.findByFileTypeAndRelatedId(fileType, relatedId).stream()
                .map(file -> FileResponse.from(file, BASE_URL))
                .toList();
    }

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileEntity file = findById(fileId);

        // 파일 시스템에서 삭제
        fileUtil.deleteFile(file.getFilePath());

        // DB에서 삭제
        fileRepository.delete(file);
    }

    @Transactional
    public void deleteFilesByTypeAndRelatedId(FileEntity.FileType fileType, Long relatedId)
            throws IOException {
        List<FileEntity> files = fileRepository.findByFileTypeAndRelatedId(fileType, relatedId);

        for (FileEntity file : files) {
            // 파일 시스템에서 삭제
            fileUtil.deleteFile(file.getFilePath());
        }

        // DB에서 삭제
        fileRepository.deleteByFileTypeAndRelatedId(fileType, relatedId);
    }
}
