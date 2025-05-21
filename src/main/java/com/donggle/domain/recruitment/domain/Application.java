package com.donggle.domain.recruitment.domain;

import com.donggle.domain.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum ApplicationStatus {
        PENDING, // 대기중
        APPROVED, // 승인됨
        REJECTED, // 거절됨
        CANCELED // 취소됨
    }

    public Application(Recruitment recruitment, User user, String content) {
        this.recruitment = recruitment;
        this.user = user;
        this.content = content;
        this.status = ApplicationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(ApplicationStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
