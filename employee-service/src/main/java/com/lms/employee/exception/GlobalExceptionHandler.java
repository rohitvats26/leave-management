package com.lms.employee.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(404).body(err(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badReq(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(err(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Validation error");
        return ResponseEntity.badRequest().body(err(400, "Bad Request", msg, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> all(Exception ex, HttpServletRequest req) {
        log.error("Error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(err(500, "Internal Server Error", ex.getMessage(), req.getRequestURI()));
    }

    private Map<String, Object> err(int s, String e, String m, String p) {
        return Map.of("timestamp", LocalDateTime.now(), "status", s, "error", e, "message", m, "path", p);
    }
}
