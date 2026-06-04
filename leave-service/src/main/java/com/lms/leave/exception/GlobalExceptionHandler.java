package com.lms.leave.exception;

import com.lms.leave.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return resp(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({ValidationException.class, InsufficientBalanceException.class})
    public ResponseEntity<ErrorResponse> badReq(RuntimeException ex, HttpServletRequest req) {
        return resp(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", ex.getMessage(), req);
    }

    @ExceptionHandler(OverlapException.class)
    public ResponseEntity<ErrorResponse> conflict(OverlapException ex, HttpServletRequest req) {
        return resp(HttpStatus.CONFLICT, "LEAVE_OVERLAP", ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenException ex, HttpServletRequest req) {
        return resp(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), req);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> svcUnavail(ServiceUnavailableException ex, HttpServletRequest req) {
        return resp(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", ex.getMessage(), req);
    }

    /**
     * Handle downstream service errors (e.g., from Feign clients).
     * Returns the exact error response from the downstream service to the client.
     */
    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ErrorResponse> downstreamError(DownstreamServiceException ex, HttpServletRequest req) {
        ErrorResponse errorResponse = ex.getErrorResponse();
        HttpStatus status = HttpStatus.resolve(errorResponse.getStatus());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if(status.value() == 503) {
            errorResponse.setMessage("service unavailable please try again letter");
        }
        log.warn("[Downstream Error] {} - {} - {}", status.value(), errorResponse.getCode(), errorResponse.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Validation error");
        return resp(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> all(Exception ex, HttpServletRequest req) {
        log.error("Error: {}", ex.getMessage(), ex);
        return resp(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> resp(HttpStatus status, String code, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(resolvePath(req))
                .traceId(resolveTraceId(req))
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String resolveTraceId(HttpServletRequest req) {
        if (req == null) {
            return UUID.randomUUID().toString();
        }
        return Optional.ofNullable(req.getHeader("X-Trace-Id"))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private String resolvePath(HttpServletRequest req) {
        return req != null ? req.getRequestURI() : "N/A";
    }
}
