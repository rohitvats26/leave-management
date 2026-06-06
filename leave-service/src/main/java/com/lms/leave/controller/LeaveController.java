package com.lms.leave.controller;

import com.lms.leave.dto.*;
import com.lms.leave.exception.ForbiddenException;
import com.lms.leave.exception.ValidationException;
import com.lms.leave.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {
    private static final String MANAGER_ACCESS_REQUIRED = "Access denied. This operation requires role ROLE_MANAGER.";
    private static final String EMPLOYEE_ACCESS_REQUIRED = "Access denied. This operation requires role ROLE_EMPLOYEE.";

    private final LeaveService leaveService;

    @PostMapping({"", "/apply"})
    public ResponseEntity<LeaveRequestResponse> apply(
            @Valid @RequestBody ApplyLeaveRequest req,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_EMPLOYEE".equals(role)) {
            throw new ForbiddenException(EMPLOYEE_ACCESS_REQUIRED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(userId, req));
    }

    @GetMapping({"/me", "/my"})
    public ResponseEntity<Page<LeaveRequestResponse>> myLeaves(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        validatePageRequest(page, size);
        return ResponseEntity.ok(leaveService.getMyLeaves(userId, status, PageRequest.of(page, size, Sort.by("appliedAt").descending())));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancel(@PathVariable String id,
                                                       @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(leaveService.cancel(parseId(id), userId));
    }

    @GetMapping({"/team/{managerId}", "/manager/{managerId}"})
    public ResponseEntity<Page<LeaveRequestResponse>> teamLeaves(@PathVariable UUID managerId,
                                                                 @RequestHeader("X-User-Role") String role,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        validatePageRequest(page, size);
        return ResponseEntity.ok(leaveService.getTeamLeaves(managerId, status, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(@PathVariable String id,
                                                        @Valid @RequestBody(required = false) ApproveRejectRequest req,
                                                        @RequestHeader("X-User-Id") String managerId,
                                                        @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.ok(leaveService.approve(parseId(id), managerId, req));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(@PathVariable String id,
                                                       @Valid @RequestBody ApproveRejectRequest req,
                                                       @RequestHeader("X-User-Id") String managerId,
                                                       @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.ok(leaveService.reject(parseId(id), managerId, req));
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid id");
        }
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
    }
}
