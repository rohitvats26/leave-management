package com.lms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DeductBalanceRequest {
    @NotBlank(message = "leaveType is required")
    private String leaveType;

    @Positive(message = "days must be greater than 0")
    private int days;
}
