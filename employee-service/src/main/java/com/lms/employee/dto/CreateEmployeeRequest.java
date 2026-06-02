package com.lms.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateEmployeeRequest {
    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @Email(message = "email must be a valid email address")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "username is required")
    private String username;

    @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "department is required")
    private String department;

    @NotNull(message = "managerId is required")
    private UUID managerId;

    @Pattern(regexp = "ROLE_(EMPLOYEE|MANAGER)", message = "role must be ROLE_EMPLOYEE or ROLE_MANAGER")
    private String role = "ROLE_EMPLOYEE";
}
