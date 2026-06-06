package com.lms.employee.controller;

import com.lms.employee.dto.*;
import com.lms.employee.exception.ForbiddenException;
import com.lms.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private static final String MANAGER_ACCESS_REQUIRED = "Access denied. This operation requires role ROLE_MANAGER.";
    private static final String EMPLOYEE_ACCESS_REQUIRED = "Access denied. ROLE_EMPLOYEE can only access their own employee record and balance.";

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest req,
                                                   @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable String id,
                                                        @RequestHeader("X-User-Id") String userId,
                                                        @RequestHeader("X-User-Role") String role) {
        UUID employeeId = parseId(id);
        if ("ROLE_EMPLOYEE".equals(role) && !userId.equals(employeeId.toString())) {
            throw new ForbiddenException(EMPLOYEE_ACCESS_REQUIRED);
        }
        return ResponseEntity.ok(employeeService.getEmployee(employeeId));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String id,
                                                          @RequestHeader("X-User-Id") String userId,
                                                          @RequestHeader("X-User-Role") String role) {
        UUID employeeId = parseId(id);
        if ("ROLE_EMPLOYEE".equals(role) && !userId.equals(employeeId.toString())) {
            throw new ForbiddenException(EMPLOYEE_ACCESS_REQUIRED);
        }
        List<LeaveBalanceDto> balances = employeeService.getBalance(employeeId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("employeeId", employeeId);
        resp.put("balances", balances);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/balance/deduct")
    public ResponseEntity<Void> deductBalance(@PathVariable String id,
                                              @Valid @RequestBody DeductBalanceRequest req) {
        employeeService.deductBalance(parseId(id), req.getLeaveType(), req.getDays());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeeResponse>> getTeam(@PathVariable UUID managerId,
                                                          @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.ok(employeeService.getTeam(managerId));
    }

    @GetMapping("/{id}/balance/check")
    public ResponseEntity<Map<String, Object>> checkBalance(@PathVariable String id,
                                                            @RequestParam String leaveType, @RequestParam int days) {
        validateLeaveBalanceCheckRequest(leaveType, days);
        String normalizedLeaveType = leaveType.trim().toUpperCase();
        int available = employeeService.getRemainingBalance(parseId(id), normalizedLeaveType);
        boolean ok = available >= days;
        return ResponseEntity.ok(Map.of("sufficient", ok, "available", available));
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    private void validateLeaveBalanceCheckRequest(String leaveType, int days) {
        if (leaveType == null || leaveType.isBlank()) {
            throw new IllegalArgumentException("leaveType is required");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("days must be greater than 0");
        }
    }
}
