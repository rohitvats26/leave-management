package com.lms.gateway.config;

import com.lms.gateway.filter.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, JwtAuthFilter jwtAuthFilter) {
        JwtAuthFilter.Config cfg = new JwtAuthFilter.Config();
        return builder.routes()

            // Auth — PUBLIC (no JWT filter)
            .route("auth-service", r -> r
                .path("/auth/**")
                .uri("lb://auth-service"))

            // Employee
            .route("employee-service", r -> r
                .path("/employees/**", "/managers/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(cfg)))
                .uri("lb://employee-service"))

            // Leave
            .route("leave-service", r -> r
                .path("/leaves/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(cfg)))
                .uri("lb://leave-service"))
            // Notification
            .route("notification-service", r -> r
                .path("/notifications/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(cfg)))
                .uri("lb://notification-service"))

            .build();
    }
}
