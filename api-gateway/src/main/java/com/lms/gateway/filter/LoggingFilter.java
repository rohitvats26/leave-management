package com.lms.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path   = exchange.getRequest().getPath().toString();
        String traceId = exchange.getRequest().getId();

        log.info(">>> [{}] {} {}", traceId, method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Spring Framework 7: getStatusCode() returns HttpStatusCode, not HttpStatus
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            long elapsed = System.currentTimeMillis() - start;
            log.info("<<< [{}] {} {} | {} | {}ms", traceId, method, path, status, elapsed);
        }));
    }

    @Override
    public int getOrder() { return -1; }
}
