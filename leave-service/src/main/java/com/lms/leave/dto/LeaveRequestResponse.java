package com.lms.leave.dto;

import lombok.*;

import java.time.*;
import java.util.UUID;

@Data
@Builder
public class LeaveRequestResponse {
    private UUID id;
    private UUID employeeId, managerId;
    private String leaveType, status, reason, rejectionReason, comments;
    private LocalDate startDate, endDate;
    private int numberOfDays;
    private LocalDateTime appliedAt, updatedAt;
}
