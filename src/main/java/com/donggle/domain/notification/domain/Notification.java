package com.donggle.domain.notification.domain;

import com.donggle.domain.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private Long relatedId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        NEW_ANNOUNCE, // 새로운 공지사항
        APPLICATION_STATUS_CHANGE, // 지원 상태 변경
        NEW_RECRUITMENT, // 새로운 모집 공고
        CLUB_MANAGER_ADDED // 동아리 관리자 추가
    }

    public Notification(
            User user, String title, String content, NotificationType type, Long relatedId) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
