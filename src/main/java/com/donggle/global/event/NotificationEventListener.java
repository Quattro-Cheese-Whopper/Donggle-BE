package com.donggle.global.event;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.club.service.ClubService;
import com.donggle.domain.notification.service.NotificationService;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserService userService;
    private final ClubService clubService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        User user = userService.findById(event.getUserId());

        notificationService.createNotification(
                user, event.getTitle(), event.getContent(), event.getType(), event.getRelatedId());
    }

    @Async
    @EventListener
    public void handleClubNotificationEvent(ClubNotificationEvent event) {
        Club club = clubService.findById(event.getClubId());

        notificationService.createNotificationForClubMembers(
                club, event.getTitle(), event.getContent(), event.getType(), event.getRelatedId());
    }
}
