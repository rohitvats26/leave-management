package com.lms.employee.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateUserRequest {
    private UUID id;
    private String username;
    private String email;
    private String password;
    private String role;
}

