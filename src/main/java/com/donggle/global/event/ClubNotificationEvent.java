package com.donggle.global.event;

import com.donggle.domain.notification.domain.Notification;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ClubNotificationEvent extends ApplicationEvent {

    private final Long clubId;
    private final String title;
    private final String content;
    private final Notification.NotificationType type;
    private final Long relatedId;

    public ClubNotificationEvent(
            Object source,
            Long clubId,
            String title,
            String content,
            Notification.NotificationType type,
            Long relatedId) {
        super(source);
        this.clubId = clubId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
    }
}
