package com.lms.auth.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import java.util.List;

@Configuration
public class TracingConfig {

    private static final List<String> IGNORED_PATHS = List.of(
            "/actuator",
            "/h2-console",
            "/eureka"
    );

    // Spring Security emits observations under these names — noisy and not useful in Zipkin
    private static final List<String> IGNORED_OBSERVATION_NAMES = List.of(
            "spring.security.filterchains",
            "spring.security.filterchain",
            "spring.security.http.chains",
            "spring.security.authorizations"
    );

    @Bean
    public ObservationPredicate skipNoisyPaths() {
        return (name, context) -> {
            // Drop by observation name (covers Security filter chain spans)
            if (IGNORED_OBSERVATION_NAMES.stream().anyMatch(name::startsWith)) {
                return false;
            }
            // Drop by request URI prefix (covers actuator, h2-console, etc.)
            if (context instanceof ServerRequestObservationContext ctx) {
                String uri = ctx.getCarrier().getRequestURI();
                return IGNORED_PATHS.stream().noneMatch(uri::startsWith);
            }
            return true;
        };
    }
}
