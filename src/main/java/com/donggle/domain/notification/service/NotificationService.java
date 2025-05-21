package com.donggle.domain.notification.service;

import com.donggle.domain.club.domain.Club;
import com.donggle.domain.notification.domain.Notification;
import com.donggle.domain.notification.dto.NotificationResponse;
import com.donggle.domain.notification.repository.NotificationRepository;
import com.donggle.domain.user.domain.User;
import com.donggle.domain.user.service.UserService;
import com.donggle.global.error.exception.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public void createNotification(
            User user,
            String title,
            String content,
            Notification.NotificationType type,
            Long relatedId) {
        Notification notification = new Notification(user, title, content, type, relatedId);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationForClubMembers(
            Club club,
            String title,
            String content,
            Notification.NotificationType type,
            Long relatedId) {
        for (User manager : club.getManagers()) {
            createNotification(manager, title, content, type, relatedId);
        }
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = findById(notificationId);
        User user = userService.findById(userId);

        // 본인의 알림인지 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);

        return NotificationResponse.from(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userService.findById(userId);
        notificationRepository.markAllAsRead(user);
    }

    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return notificationRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        Notification notification = findById(notificationId);

        // 본인의 알림인지 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }

        return NotificationResponse.from(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        User user = userService.findById(userId);

        return notificationRepository.findByUser(user, pageable).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        User user = userService.findById(userId);

        return notificationRepository
                .findByUserAndIsRead(user, false, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        User user = userService.findById(userId);
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentUnreadNotifications(Long userId) {
        User user = userService.findById(userId);

        return notificationRepository
                .findTop5ByUserAndIsReadOrderByCreatedAtDesc(user, false)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
