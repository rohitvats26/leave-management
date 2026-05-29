package com.lms.leave.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "employee-service", fallback = EmployeeClientFallback.class)
public interface EmployeeClient {

    @GetMapping("/employees/{id}/balance/check")
    ResponseEntity<Map<String, Object>> checkBalance(@PathVariable UUID id,
                                                     @RequestParam String leaveType, @RequestParam int days);

    @PutMapping("/employees/{id}/balance/deduct")
    ResponseEntity<Void> deductBalance(@PathVariable UUID id, @RequestBody DeductRequest body);

    @Data
    class DeductRequest {
        private String leaveType;
        private int days;
    }
}
