package com.lms.leave.controller;

import com.lms.leave.dto.*;
import com.lms.leave.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping("/apply")
    public ResponseEntity<LeaveRequestResponse> apply(
            @Valid @RequestBody ApplyLeaveRequest req,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_EMPLOYEE".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(userId, req));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<LeaveRequestResponse>> myLeaves(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getMyLeaves(userId, status, PageRequest.of(page, size, Sort.by("appliedAt").descending())));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancel(@PathVariable UUID id,
                                                       @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(leaveService.cancel(id, userId));
    }

    @GetMapping("/team/{managerId}")
    public ResponseEntity<Page<LeaveRequestResponse>> teamLeaves(@PathVariable UUID managerId,
                                                                 @RequestHeader("X-User-Role") String role,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(leaveService.getTeamLeaves(managerId, status, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(@PathVariable UUID id,
                                                        @RequestBody(required = false) ApproveRejectRequest req,
                                                        @RequestHeader("X-User-Id") String managerId,
                                                        @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(leaveService.approve(id, managerId, req));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(@PathVariable UUID id,
                                                       @RequestBody ApproveRejectRequest req,
                                                       @RequestHeader("X-User-Id") String managerId,
                                                       @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(leaveService.reject(id, managerId, req));
    }
}
