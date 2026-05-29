package com.lms.leave.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ApplyLeaveRequest {
    @NotBlank
    private String leaveType;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @Min(1)
    private int numberOfDays;
    @NotBlank
    @Size(min = 10, max = 500)
    private String reason;
    @NotNull
    private UUID managerId;
}
