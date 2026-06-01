package com.lms.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private UUID recipientId;
    private String recipientType; // EMPLOYEE | MANAGER
    @Column(nullable = false, length = 1000)
    private String message;
    private UUID leaveRequestId;
    @Builder.Default
    private boolean isRead = false;
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
