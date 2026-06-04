package com.lms.leave.exception;

import com.lms.leave.dto.ErrorResponse;
import lombok.Getter;

/**
 * Exception wrapping error responses from downstream services (e.g., Employee Service via Feign).
 * Allows passing the original error response directly to the user.
 */
@Getter
public class DownstreamServiceException extends RuntimeException {
    private final ErrorResponse errorResponse;

    public DownstreamServiceException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public DownstreamServiceException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }
}

