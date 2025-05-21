package com.donggle.global.event;

import com.donggle.domain.notification.domain.Notification;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long userId;
    private final String title;
    private final String content;
    private final Notification.NotificationType type;
    private final Long relatedId;

    public NotificationEvent(
            Object source,
            Long userId,
            String title,
            String content,
            Notification.NotificationType type,
            Long relatedId) {
        super(source);
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
    }
}
