package com.lms.auth.controller;

import com.lms.auth.dto.*;
import com.lms.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private static final String MANAGER_ACCESS_REQUIRED = "Access denied. This operation requires role ROLE_MANAGER.";

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
                                                   @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        return ResponseEntity.status(201).body(authService.createUser(request));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username,
                                           @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_MANAGER".equals(role)) {
            throw new ForbiddenException(MANAGER_ACCESS_REQUIRED);
        }
        authService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
