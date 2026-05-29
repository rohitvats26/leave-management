package com.lms.auth.service;

import com.lms.auth.dto.*;
import com.lms.auth.entity.User;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!user.isEnabled()) throw new BadCredentialsException("Account is disabled");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid username or password");

        String token = jwtService.generateToken(user.getId().toString(), user.getUsername(), user.getRole());
        log.info("Login successful for user: {}", user.getUsername());
        return LoginResponse.builder()
                .token(token).type("Bearer")
                .userId(user.getId().toString())
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(86400L)
                .build();
    }
}
