package com.lms.employee.controller;

import com.lms.employee.dto.*;
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

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest req,
                                                   @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable UUID id,
                                                        @RequestHeader("X-User-Id") String userId,
                                                        @RequestHeader("X-User-Role") String role) {
        if ("ROLE_EMPLOYEE".equals(role) && !userId.equals(id.toString()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable UUID id,
                                                          @RequestHeader("X-User-Id") String userId,
                                                          @RequestHeader("X-User-Role") String role) {
        if ("ROLE_EMPLOYEE".equals(role) && !userId.equals(id.toString()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<LeaveBalanceDto> balances = employeeService.getBalance(id);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("employeeId", id);
        resp.put("balances", balances);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/balance/deduct")
    public ResponseEntity<Void> deductBalance(@PathVariable UUID id,
                                              @RequestBody DeductBalanceRequest req) {
        employeeService.deductBalance(id, req.getLeaveType(), req.getDays());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeeResponse>> getTeam(@PathVariable UUID managerId,
                                                          @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(employeeService.getTeam(managerId));
    }

    @GetMapping("/{id}/balance/check")
    public ResponseEntity<Map<String, Object>> checkBalance(@PathVariable UUID id,
                                                            @RequestParam String leaveType, @RequestParam int days) {
        boolean ok = employeeService.hasEnoughBalance(id, leaveType, days);
        return ResponseEntity.ok(Map.of("sufficient", ok));
    }
}
