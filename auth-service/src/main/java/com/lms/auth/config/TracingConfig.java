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

    @Bean
    public ObservationPredicate skipNoisyPaths() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext ctx) {
                String uri = ctx.getCarrier().getRequestURI();
                return IGNORED_PATHS.stream().noneMatch(uri::startsWith);
            }
            return true;
        };
    }
}
