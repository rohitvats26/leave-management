package com.lms.manager.dto;

import lombok.Data;

@Data
public class ApproveRejectRequest {
    private String comments;
    private String rejectionReason;
}
