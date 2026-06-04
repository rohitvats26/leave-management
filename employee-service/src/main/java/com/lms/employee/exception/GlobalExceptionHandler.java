package com.lms.employee.exception;

import com.lms.employee.dto.ErrorResponse;
import com.lms.leave.exception.DownstreamServiceException;
import feign.FeignException;
import feign.RetryableException;
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

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException ex, HttpServletRequest req) {
        return resp(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badReq(IllegalArgumentException ex, HttpServletRequest req) {
        return resp(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), req);
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
        return resp(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Request could not be completed due to an unexpected server error", req);
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ErrorResponse> downstreamError(DownstreamServiceException ex, HttpServletRequest req) {
        ErrorResponse errorResponse = ex.getErrorResponse();
        HttpStatus status = HttpStatus.resolve(errorResponse.getStatus());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        log.warn("[Downstream Error] {} - {} - {}", status.value(), errorResponse.getCode(), errorResponse.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> dependencyUnavailable(RetryableException ex, HttpServletRequest req) {
        log.warn("Dependency unavailable: {}", ex.getMessage());
        return resp(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE",
                "Dependent service is temporarily unavailable", req);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> feignFallback(FeignException ex, HttpServletRequest req) {
        HttpStatus status = resolveStatus(ex.status());
        String message = status.is5xxServerError()
                ? "A required service is temporarily unavailable. Please try again."
                : "Request could not be completed because of a dependent service error.";
        return resp(status, "DOWNSTREAM_ERROR", message, req);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenException ex, HttpServletRequest req) {
        return resp(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), req);
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

    private HttpStatus resolveStatus(int statusCode) {
        HttpStatus resolved = HttpStatus.resolve(statusCode);
        if (resolved == null || resolved.is5xxServerError()) {
            return HttpStatus.BAD_GATEWAY;
        }
        return resolved;
    }
}
