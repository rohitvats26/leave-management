package com.lms.notification.repository;

import com.lms.notification.entity.NotificationLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationLog, UUID> {
    Page<NotificationLog> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);
}
