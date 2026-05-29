package com.lms.employee.dto;

import lombok.Data;

@Data
public class DeductBalanceRequest {
    private String leaveType;
    private int days;
}
