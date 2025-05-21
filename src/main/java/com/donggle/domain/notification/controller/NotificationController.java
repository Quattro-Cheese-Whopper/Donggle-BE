package com.donggle.domain.notification.controller;

import com.donggle.domain.notification.dto.NotificationResponse;
import com.donggle.domain.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Pageable pageable, Long userId) {

        Page<NotificationResponse> response =
                notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            Pageable pageable, Long userId) {

        Page<NotificationResponse> response =
                notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationResponse> getNotification(Long notificationId, Long userId) {

        NotificationResponse response = notificationService.getNotification(notificationId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationResponse> markAsRead(Long notificationId, Long userId) {

        NotificationResponse response = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> markAllAsRead(Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Long> countUnreadNotifications(Long userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    @Override
    public ResponseEntity<List<NotificationResponse>> getRecentUnreadNotifications(Long userId) {
        List<NotificationResponse> response =
                notificationService.getRecentUnreadNotifications(userId);
        return ResponseEntity.ok(response);
    }
}
