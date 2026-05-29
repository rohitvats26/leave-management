package com.lms.leave.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class EmployeeClientFallback implements EmployeeClient {

    @Override
    public ResponseEntity<Map<String, Object>> checkBalance(UUID id, String leaveType, int days) {
        log.error("Employee service unavailable - balance check fallback");
        return ResponseEntity.status(503).body(Map.of("sufficient", false, "fallback", true));
    }

    @Override
    public ResponseEntity<Void> deductBalance(UUID id, DeductRequest body) {
        log.error("Employee service unavailable - deduct fallback");
        return ResponseEntity.status(503).build();
    }
}
