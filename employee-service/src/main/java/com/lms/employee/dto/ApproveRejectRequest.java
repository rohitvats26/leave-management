package com.lms.employee.dto;

import lombok.Data;

@Data
public class ApproveRejectRequest {
    private String comments;
    private String rejectionReason;
}
