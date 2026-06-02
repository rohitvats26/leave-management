package com.lms.auth.service;

import com.lms.auth.dto.*;
import com.lms.auth.entity.User;
import com.lms.auth.exception.ConflictException;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("This username is already in use. Please choose a different username.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("This email is already registered. Please use a different email address.");
        }

        User user = User.builder()
                .id(request.getId())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created login user for username={}", savedUser.getUsername());
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .enabled(savedUser.isEnabled())
                .build();
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
        log.warn("Deleted login user for username={} as part of compensation", username);
    }

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
