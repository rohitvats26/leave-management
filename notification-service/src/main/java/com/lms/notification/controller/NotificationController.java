package com.lms.notification.controller;

import com.lms.notification.entity.NotificationLog;
import com.lms.notification.exception.ResourceNotFoundException;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;

    @GetMapping({"/me", "/my"})
    public ResponseEntity<Page<NotificationLog>> myNotifications(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        validatePageRequest(page, size);
        return ResponseEntity.ok(repo.findByRecipientIdOrderByCreatedAtDesc(
                UUID.fromString(userId), PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationLog> getById(@PathVariable UUID id) {
        NotificationLog notification = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationLog> markRead(@PathVariable UUID id) {
        NotificationLog notification = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        notification.setRead(true);
        return ResponseEntity.ok(repo.save(notification));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
    }
}
