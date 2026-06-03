package com.lms.gateway.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start  = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path   = exchange.getRequest().getPath().toString();

        Span currentSpan = tracer.currentSpan();
        String traceId = (currentSpan != null)
                ? currentSpan.context().traceId()
                : exchange.getRequest().getId();

        log.info(">>> [traceId={}] {} {}", traceId, method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpStatusCode status  = exchange.getResponse().getStatusCode();
            long elapsed = System.currentTimeMillis() - start;
            log.info("<<< [traceId={}] {} {} | {} | {}ms", traceId, method, path, status, elapsed);
        }));
    }

    @Override
    public int getOrder() { return -1; }
}
