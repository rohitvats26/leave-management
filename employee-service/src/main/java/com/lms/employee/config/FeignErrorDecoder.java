package com.lms.employee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.employee.dto.ErrorResponse;
import com.lms.leave.exception.DownstreamServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Custom Feign Error Decoder that extracts error response bodies from downstream services
 * and wraps them in DownstreamServiceException for direct propagation to the client.
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Try to parse the response body as ErrorResponse
            String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            if (body == null || body.isBlank()) {
                // If no body, create a generic error response
                return createGenericException(methodKey, response);
            }

            try {
                // Attempt to deserialize as ErrorResponse
                ErrorResponse errorResponse = objectMapper.readValue(body, ErrorResponse.class);
                log.warn("[Feign] Downstream service error: {} - {} - {}", 
                    response.status(), errorResponse.getCode(), errorResponse.getMessage());
                return new DownstreamServiceException(errorResponse);
            } catch (Exception e) {
                // If deserialization fails, try to parse as JSON and create ErrorResponse
                log.warn("[Feign] Could not deserialize error response, creating generic error: {}", body);
                return createGenericException(methodKey, response, body);
            }
        } catch (IOException e) {
            log.error("[Feign] Error reading response body: {}", e.getMessage());
            return createGenericException(methodKey, response);
        }
    }

    private Exception createGenericException(String methodKey, Response response) {
        return createGenericException(methodKey, response, null);
    }

    private Exception createGenericException(String methodKey, Response response, String body) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(response.status())
                .error(response.reason() != null ? response.reason() : "Unknown Error")
                .code("DOWNSTREAM_ERROR")
                .message(body != null ? body : "An error occurred in downstream service")
                .path("N/A")
                .traceId("N/A")
                .build();
        return new DownstreamServiceException(errorResponse);
    }
}

