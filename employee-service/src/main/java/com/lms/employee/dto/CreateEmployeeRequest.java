package com.lms.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateEmployeeRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String department;
    @NotNull
    private UUID managerId;
    private String role = "ROLE_EMPLOYEE";
}
