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
    private UUID leaveRequestId, employeeId, managerId;
    private String leaveType, status, reason, rejectionReason, comments;
    private LocalDate startDate, endDate;
    private int numberOfDays;
    private LocalDateTime timestamp;
}
