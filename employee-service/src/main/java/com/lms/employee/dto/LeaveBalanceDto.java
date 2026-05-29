package com.lms.employee.dto;

import lombok.*;

@Data
@Builder
public class LeaveBalanceDto {
    private String leaveType;
    private int allocated;
    private int used;
    private int remaining;
}
