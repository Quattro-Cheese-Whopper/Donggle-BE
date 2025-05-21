package com.donggle.domain.notification.dto;

import com.donggle.domain.notification.domain.Notification;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;
    private Notification.NotificationType type;
    private Long relatedId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getRelatedId(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
