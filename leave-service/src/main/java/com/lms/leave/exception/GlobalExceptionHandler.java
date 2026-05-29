package com.lms.leave.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return resp(404, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler({ValidationException.class, InsufficientBalanceException.class})
    public ResponseEntity<Map<String, Object>> badReq(RuntimeException ex, HttpServletRequest req) {
        return resp(400, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(OverlapException.class)
    public ResponseEntity<Map<String, Object>> conflict(OverlapException ex, HttpServletRequest req) {
        return resp(409, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> forbidden(ForbiddenException ex, HttpServletRequest req) {
        return resp(403, "Forbidden", ex.getMessage(), req);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> svcUnavail(ServiceUnavailableException ex, HttpServletRequest req) {
        return resp(503, "Service Unavailable", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Validation error");
        return resp(400, "Bad Request", msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> all(Exception ex, HttpServletRequest req) {
        log.error("Error: {}", ex.getMessage(), ex);
        return resp(500, "Internal Server Error", ex.getMessage(), req);
    }

    private ResponseEntity<Map<String, Object>> resp(int s, String e, String m, HttpServletRequest req) {
        return ResponseEntity.status(s).body(Map.of("timestamp", LocalDateTime.now(), "status", s, "error", e, "message", m, "path", req.getRequestURI()));
    }
}
