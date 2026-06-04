package com.lms.leave.service;

import com.lms.leave.client.EmployeeClient;
import com.lms.leave.exception.DownstreamServiceException;
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
        Map<String, Object> body = response.getBody();
        return body != null && Boolean.TRUE.equals(body.get("sufficient"));
    }

    // Fallback — triggered when circuit is OPEN or all retries exhausted
    public boolean checkBalanceFallback(UUID employeeId, String leaveType, int days, Throwable t) {
        log.error("[CB FALLBACK] checkBalance failed for employee={}: {}", employeeId, t.getMessage());
        // Let DownstreamServiceException propagate if it's raised by FeignErrorDecoder
        if (t instanceof DownstreamServiceException) {
            throw (DownstreamServiceException) t;
        }
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
        // Let DownstreamServiceException propagate if it's raised by FeignErrorDecoder
        if (t instanceof DownstreamServiceException) {
            throw (com.lms.leave.exception.DownstreamServiceException) t;
        }
        throw new ServiceUnavailableException(
            "Employee service is currently unavailable. Leave approval deferred.");
    }

    @CircuitBreaker(name = "employee-service", fallbackMethod = "getAvailableBalanceFallback")
    @Retry(name = "employee-service")
    public int getAvailableBalance(UUID employeeId, String leaveType, int days) {
        log.debug("[CB] getAvailableBalance employeeId={} leaveType={} days={}", employeeId, leaveType, days);
        ResponseEntity<Map<String, Object>> response =
                employeeClient.checkBalance(employeeId, leaveType, days);

        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("Unable to verify leave balance because employee service is unavailable.");
        }

        Object availableObj = response.getBody() != null ? response.getBody().get("available") : null;
        if (availableObj instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    public int getAvailableBalanceFallback(UUID employeeId, String leaveType, int days, Throwable t) {
        log.error("[CB FALLBACK] getAvailableBalance failed for employee={}: {}", employeeId, t.getMessage());
        if (t instanceof com.lms.leave.exception.DownstreamServiceException) {
            throw (com.lms.leave.exception.DownstreamServiceException) t;
        }
        throw new ServiceUnavailableException(
                "Employee service is currently unavailable. Cannot verify leave balance.");
    }
}
