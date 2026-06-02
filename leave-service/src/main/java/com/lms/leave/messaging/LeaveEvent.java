package com.lms.leave.messaging;

import lombok.*;

import java.time.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveEvent {
    private String eventType;
    private UUID leaveRequestId;
    private UUID employeeId;
    private UUID managerId;
    private String leaveType;
    private String status;
    private String reason;
    private String rejectionReason;
    private String comments;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private LocalDateTime timestamp;
}
