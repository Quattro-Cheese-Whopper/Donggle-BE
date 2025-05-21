package com.donggle.domain.notification.repository;

import com.donggle.domain.notification.domain.Notification;
import com.donggle.domain.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser(User user, Pageable pageable);

    Page<Notification> findByUserAndIsRead(User user, boolean isRead, Pageable pageable);

    long countByUserAndIsRead(User user, boolean isRead);

    List<Notification> findTop5ByUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    void markAllAsRead(@Param("user") User user);
}
