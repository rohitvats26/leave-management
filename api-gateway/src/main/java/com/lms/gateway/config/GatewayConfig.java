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
                // Auth Public Endpoint
                .route("auth-public-service", r -> r
                        .path("/auth/actuator/**").filters(f -> f.stripPrefix(1))
                        .uri("lb://auth-service"))

                // Employee Public Endpoint
                .route("employee-public-service", r -> r
                        .path("/employees/actuator/**", "/manager/actuator/**").filters(f -> f.stripPrefix(1))
                        .uri("lb://employee-service"))

                // Leave Public Endpoint
                .route("leave-public-service", r -> r
                        .path("/leaves/actuator/**").filters(f -> f.stripPrefix(1))
                        .uri("lb://leave-service"))

                // Notification Public Endpoint
                .route("leave-public-service", r -> r
                        .path("/notifications/actuator/**").filters(f -> f.stripPrefix(1))
                        .uri("lb://notification-service"))

                // Auth
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))

                // Employee
                .route("employee-service", r -> r
                        .path("/employees/**", "/manager/**")
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
