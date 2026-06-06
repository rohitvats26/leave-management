package com.lms.employee.controller;

import com.lms.employee.client.LeaveClient;
import com.lms.employee.dto.ApproveRejectRequest;
import com.lms.employee.exception.ForbiddenException;
import com.lms.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private static final String MANAGER_ACCESS_REQUIRED = "Access denied. This operation requires role ROLE_MANAGER.";
    private final LeaveClient leaveClient;
    private final EmployeeService employeeService;

    @GetMapping("/team/requests")
    public ResponseEntity<?> getTeamRequests(
            @RequestHeader("X-User-Id") String managerId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        validatePageRequest(page, size);
        return leaveClient.getTeamLeaves(UUID.fromString(managerId), status, page, size);
    }

    @GetMapping("/team/requests/pending")
    public ResponseEntity<?> getPending(@RequestHeader("X-User-Id") String managerId,
                                        @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return leaveClient.getTeamLeaves(UUID.fromString(managerId), "PENDING", 0, 50);
    }

    @PostMapping("/leaves/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id,
                                     @Valid @RequestBody(required = false) ApproveRejectRequest req,
                                     @RequestHeader("X-User-Id") String managerId,
                                     @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return leaveClient.approveLeave(parseId(id), req, managerId, role);
    }

    @PostMapping("/leaves/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id,
                                    @Valid @RequestBody ApproveRejectRequest req,
                                    @RequestHeader("X-User-Id") String managerId,
                                    @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return leaveClient.rejectLeave(parseId(id), req, managerId, role);
    }

    @GetMapping("/team/employees")
    public ResponseEntity<?> getTeam(@RequestHeader("X-User-Id") String managerId,
                                     @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.ok(employeeService.getTeam(UUID.fromString(managerId)));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid id");
        }
    }
}
