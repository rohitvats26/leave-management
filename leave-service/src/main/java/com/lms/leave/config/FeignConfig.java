package com.lms.leave.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignHeaderInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            Collections.list(request.getHeaderNames())
                    .forEach(headerName -> requestTemplate.header(headerName, request.getHeader(headerName)));
        };
    }
}