package com.lms.employee.client;

import com.lms.employee.config.FeignConfig;
import com.lms.employee.dto.ApproveRejectRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "leave-service", configuration = FeignConfig.class)
public interface LeaveClient {
    @GetMapping("/leaves/team/{managerId}")
    ResponseEntity<Map<String, Object>> getTeamLeaves(@PathVariable UUID managerId,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size);

    @PutMapping("/leaves/{id}/approve")
    ResponseEntity<Map<String, Object>> approveLeave(@PathVariable UUID id,
                                                     @RequestBody(required = false) ApproveRejectRequest req,
                                                     @RequestHeader("X-User-Id") String managerId,
                                                     @RequestHeader("X-User-Role") String role);

    @PutMapping("/leaves/{id}/reject")
    ResponseEntity<Map<String, Object>> rejectLeave(@PathVariable UUID id,
                                                    @RequestBody ApproveRejectRequest req,
                                                    @RequestHeader("X-User-Id") String managerId,
                                                    @RequestHeader("X-User-Role") String role);
}
