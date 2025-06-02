package com.donggle.domain.file.domain;

import com.donggle.global.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storedName;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false)
    private Long relatedId;

    public enum FileType {
        CLUB_IMAGE, // 동아리 이미지
        RECRUITMENT_IMAGE, // 모집 공고 이미지
        ANNOUNCE_ATTACHMENT // 공지사항 첨부파일
    }

    public FileEntity(
            String originalName,
            String storedName,
            String extension,
            String contentType,
            Long size,
            String filePath,
            FileType fileType,
            Long relatedId) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.extension = extension;
        this.contentType = contentType;
        this.size = size;
        this.filePath = filePath;
        this.fileType = fileType;
        this.relatedId = relatedId;
    }
}
