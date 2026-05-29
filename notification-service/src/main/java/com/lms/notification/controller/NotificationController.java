package com.lms.notification.controller;

import com.lms.notification.entity.NotificationLog;
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

    @GetMapping("/my")
    public ResponseEntity<Page<NotificationLog>> myNotifications(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repo.findByRecipientIdOrderByCreatedAtDesc(
                UUID.fromString(userId), PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationLog> getById(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationLog> markRead(@PathVariable UUID id) {
        return repo.findById(id).map(n -> {
            n.setRead(true);
            return ResponseEntity.ok(repo.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }
}
