package com.lms.leave.dto;

import lombok.Data;

@Data
public class ApproveRejectRequest {
    private String comments;
    private String rejectionReason;
}
