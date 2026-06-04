package com.lms.employee.exception;

import com.lms.employee.dto.ErrorResponse;
import lombok.Getter;

/**
 * Exception wrapping error responses from downstream services (e.g., Auth Service via Feign).
 * Allows passing the original error response directly to the user.
 */
@Getter
public class DownstreamServiceException extends RuntimeException {
    private final ErrorResponse errorResponse;
    private int status;
    private String code;

    public DownstreamServiceException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.status = errorResponse.getStatus();
        this.code = errorResponse.getCode();
        this.errorResponse = errorResponse;
    }

    public DownstreamServiceException(String message, ErrorResponse errorResponse) {
        super(message);
        this.status = errorResponse.getStatus();
        this.code = errorResponse.getCode();
        this.errorResponse = errorResponse;
    }
}

