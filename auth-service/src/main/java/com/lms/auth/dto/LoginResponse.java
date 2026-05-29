package com.lms.auth.dto;

import lombok.*;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String type;
    private String userId;
    private String username;
    private String role;
    private long expiresIn;
}
