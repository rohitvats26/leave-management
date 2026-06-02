package com.lms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateUserRequest {
    @NotNull(message = "id is required")
    private UUID id;

    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "email must be a valid email address")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
    private String password;

    @Pattern(regexp = "ROLE_(EMPLOYEE|MANAGER)", message = "role must be ROLE_EMPLOYEE or ROLE_MANAGER")
    private String role = "ROLE_EMPLOYEE";
}

