package com.lms.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> PUBLIC_PATTERN = List.of("/auth/login", "/**/actuator/health", "/h2-console/**");

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    @NonNull
    public GatewayFilter apply(@NonNull Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            if (PUBLIC_PATTERN.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) return chain.filter(exchange);
            log.debug("JwtAuthFilter processing path: {}", path);

            String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Missing or malformed header
            if (header == null || !header.startsWith("Bearer ")) {
                log.warn("Missing/invalid Authorization header for: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            String token = header.substring(7).trim();
            if (token.isEmpty()) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Empty JWT token");
            }

            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();
                String role   = claims.get("role", String.class);

                log.info("JWT valid | userId={} role={} path={}", userId, role, path);

                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id",       userId)
                                .header("X-User-Role",     role)
                                .header("X-User-Username", claims.get("username", String.class)))
                        .build();

                return chain.filter(mutated);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT expired for path {}: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT token has expired");
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.warn("JWT signature invalid for path {}: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
            } catch (Exception e) {
                log.error("JWT validation error for path {}: {}", path, e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT validation failed");
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message
        );

        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {}
}
