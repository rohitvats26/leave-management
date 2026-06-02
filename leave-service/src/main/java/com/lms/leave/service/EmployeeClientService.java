package com.lms.leave.service;

import com.lms.leave.client.EmployeeClient;
import com.lms.leave.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Wraps EmployeeClient calls with Resilience4j Circuit Breaker + Retry.
 *
 * Circuit Breaker "employee-service":
 *   CLOSED  → requests flow normally
 *   OPEN    → calls fail immediately with fallback (no network attempt)
 *   HALF-OPEN → limited probe calls to test recovery
 *
 * Retry "employee-service":
 *   Up to 3 attempts with 500ms wait before tripping the circuit breaker.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeClientService {

    private final EmployeeClient employeeClient;

    @CircuitBreaker(name = "employee-service", fallbackMethod = "checkBalanceFallback")
    @Retry(name = "employee-service")
    public boolean checkBalance(UUID employeeId, String leaveType, int days) {
        log.debug("[CB] checkBalance employeeId={} leaveType={} days={}", employeeId, leaveType, days);
        ResponseEntity<Map<String, Object>> response =
            employeeClient.checkBalance(employeeId, leaveType, days);

        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("Unable to verify leave balance because employee service is unavailable.");
        }
        return Boolean.TRUE.equals(response.getBody().get("sufficient"));
    }

    // Fallback — triggered when circuit is OPEN or all retries exhausted
    public boolean checkBalanceFallback(UUID employeeId, String leaveType, int days, Throwable t) {
        log.error("[CB FALLBACK] checkBalance failed for employee={}: {}", employeeId, t.getMessage());
        throw new ServiceUnavailableException(
            "Employee service is currently unavailable. Cannot verify leave balance.");
    }

    @CircuitBreaker(name = "employee-service", fallbackMethod = "deductBalanceFallback")
    @Retry(name = "employee-service")
    public void deductBalance(UUID employeeId, String leaveType, int days) {
        log.debug("[CB] deductBalance employeeId={} leaveType={} days={}", employeeId, leaveType, days);
        EmployeeClient.DeductRequest req = new EmployeeClient.DeductRequest();
        req.setLeaveType(leaveType);
        req.setDays(days);
        ResponseEntity<Void> response = employeeClient.deductBalance(employeeId, req);
        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("Unable to update leave balance because employee service is unavailable.");
        }
    }

    public void deductBalanceFallback(UUID employeeId, String leaveType, int days, Throwable t) {
        log.error("[CB FALLBACK] deductBalance failed for employee={}: {}", employeeId, t.getMessage());
        throw new ServiceUnavailableException(
            "Employee service is currently unavailable. Leave approval deferred.");
    }
}
