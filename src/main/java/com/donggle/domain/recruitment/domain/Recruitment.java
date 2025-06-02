package com.donggle.domain.recruitment.domain;

import com.donggle.domain.club.domain.Club;
import com.donggle.global.persistence.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruitments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Recruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer recruitCount;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentStatus status;

    private String contactInfo;

    private String applicationLink;

    public enum RecruitmentStatus {
        RECRUITING, // 모집중
        COMPLETED, // 모집완료
        ALWAYS_RECRUITING // 상시모집
    }

    public Recruitment(
            Club club,
            String title,
            String content,
            Integer recruitCount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            RecruitmentStatus status,
            String contactInfo,
            String applicationLink) {
        this.club = club;
        this.title = title;
        this.content = content;
        this.recruitCount = recruitCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.contactInfo = contactInfo;
        this.applicationLink = applicationLink;
    }

    public void update(
            String title,
            String content,
            Integer recruitCount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            RecruitmentStatus status,
            String contactInfo,
            String applicationLink) {
        this.title = title;
        this.content = content;
        this.recruitCount = recruitCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.contactInfo = contactInfo;
        this.applicationLink = applicationLink;
    }

    public void updateStatus(RecruitmentStatus status) {
        this.status = status;
    }
}
