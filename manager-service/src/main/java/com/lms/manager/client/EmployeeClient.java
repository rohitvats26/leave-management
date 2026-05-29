package com.lms.manager.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@FeignClient(name = "employee-service")
public interface EmployeeClient {
    @GetMapping("/employees/manager/{managerId}")
    ResponseEntity<List<Map<String, Object>>> getTeam(@PathVariable UUID managerId,
                                                      @RequestHeader("X-User-Role") String role);
}
