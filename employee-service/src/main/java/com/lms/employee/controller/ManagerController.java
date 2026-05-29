package com.lms.employee.controller;

import com.lms.employee.client.LeaveClient;
import com.lms.employee.dto.ApproveRejectRequest;
import com.lms.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final LeaveClient leaveClient;
    private final EmployeeService employeeService;

    @GetMapping("/team/requests")
    public ResponseEntity<?> getTeamRequests(
            @RequestHeader("X-User-Id") String managerId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(403).build();
        return leaveClient.getTeamLeaves(UUID.fromString(managerId), status, page, size);
    }

    @GetMapping("/team/requests/pending")
    public ResponseEntity<?> getPending(@RequestHeader("X-User-Id") String managerId,
                                        @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(403).build();
        return leaveClient.getTeamLeaves(UUID.fromString(managerId), "PENDING", 0, 50);
    }

    @PostMapping("/leaves/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable UUID id,
                                     @RequestBody(required = false) ApproveRejectRequest req,
                                     @RequestHeader("X-User-Id") String managerId,
                                     @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(403).build();
        return leaveClient.approveLeave(id, req, managerId, role);
    }

    @PostMapping("/leaves/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id,
                                    @RequestBody ApproveRejectRequest req,
                                    @RequestHeader("X-User-Id") String managerId,
                                    @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(403).build();
        return leaveClient.rejectLeave(id, req, managerId, role);
    }

    @GetMapping("/team/employees")
    public ResponseEntity<?> getTeam(@RequestHeader("X-User-Id") String managerId,
                                     @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(employeeService.getTeam(UUID.fromString(managerId)));
    }
}
