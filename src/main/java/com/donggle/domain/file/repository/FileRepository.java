package com.donggle.domain.file.repository;

import com.donggle.domain.file.domain.FileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByFileTypeAndRelatedId(FileEntity.FileType fileType, Long relatedId);

    Optional<FileEntity> findByStoredName(String storedName);

    void deleteByFileTypeAndRelatedId(FileEntity.FileType fileType, Long relatedId);
}
