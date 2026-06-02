package com.lms.leave.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApproveRejectRequest {
    @Size(max = 500, message = "comments must not exceed 500 characters")
    private String comments;

    @Size(max = 500, message = "rejectionReason must not exceed 500 characters")
    private String rejectionReason;
}
