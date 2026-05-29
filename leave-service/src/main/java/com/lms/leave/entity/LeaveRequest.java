package com.lms.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID employeeId;
    @Column(nullable = false)
    private UUID managerId;
    @Column(nullable = false)
    private String leaveType;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false)
    private int numberOfDays;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(nullable = false)
    private String status; // PENDING,APPROVED,REJECTED,CANCELLED
    private String rejectionReason;
    private String comments;
    @Column(updatable = false)
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
