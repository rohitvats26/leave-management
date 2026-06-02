package com.lms.employee.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.employee.exception.DownstreamServiceException;
import feign.RequestInterceptor;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            Collections.list(request.getHeaderNames()).stream()
                    .filter(headerName -> !"content-length".equalsIgnoreCase(headerName))
                    .forEach(headerName -> requestTemplate.header(headerName, request.getHeader(headerName)));
        };
    }

    @Bean
    public ErrorDecoder feignErrorDecoder(ObjectMapper objectMapper) {
        return (methodKey, response) -> toDownstreamException(methodKey, response, objectMapper);
    }

    private Exception toDownstreamException(String methodKey, Response response, ObjectMapper objectMapper) {
        String clientName = extractClientName(methodKey);
        String operationName = extractOperationName(methodKey);
        String rawBody = readBody(response);
        String errorCode = defaultCode(clientName);
        String errorMessage = "Dependent service request failed";

        if (!rawBody.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rawBody);
                errorCode = readText(root, "code", errorCode);
                errorMessage = readText(root, "message", errorMessage);
            } catch (IOException parseError) {
                errorMessage = rawBody;
            }
        }

        if (errorMessage.isBlank()) {
            errorMessage = "Dependent service request failed with status " + response.status();
        }

        String message = userFriendlyMessage(clientName, operationName, response.status(), errorMessage);
        return new DownstreamServiceException(response.status(), errorCode, message);
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try {
            return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return "";
        }
    }

    private String readText(JsonNode root, String field, String fallback) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        String text = value.asText();
        return text != null && !text.isBlank() ? text : fallback;
    }

    private String extractClientName(String methodKey) {
        if (methodKey == null || methodKey.isBlank()) {
            return "unknown-client";
        }
        int hashIndex = methodKey.indexOf('#');
        String left = hashIndex >= 0 ? methodKey.substring(0, hashIndex) : methodKey;
        int dotIndex = left.lastIndexOf('.');
        return dotIndex >= 0 ? left.substring(dotIndex + 1) : left;
    }

    private String extractOperationName(String methodKey) {
        if (methodKey == null || methodKey.isBlank()) {
            return "unknown-operation";
        }
        int hashIndex = methodKey.indexOf('#');
        if (hashIndex < 0 || hashIndex + 1 >= methodKey.length()) {
            return "unknown-operation";
        }
        String right = methodKey.substring(hashIndex + 1);
        int bracketIndex = right.indexOf('(');
        return bracketIndex >= 0 ? right.substring(0, bracketIndex) : right;
    }

    private String defaultCode(String clientName) {
        return switch (clientName) {
            case "AuthClient" -> "AUTH_SERVICE_ERROR";
            case "LeaveClient" -> "LEAVE_SERVICE_ERROR";
            default -> "DOWNSTREAM_ERROR";
        };
    }

    private String userFriendlyMessage(String clientName, String operationName, int status, String downstreamMessage) {
        if ("AuthClient".equals(clientName) && "createUser".equals(operationName)) {
            if (status == 409) {
                return "Unable to create employee login account because the username or email already exists.";
            }
            if (status >= 500) {
                return "Employee profile could not be completed because the authentication service is unavailable. Please try again.";
            }
            return "Unable to create employee login account: " + downstreamMessage;
        }

        if ("AuthClient".equals(clientName) && "deleteUser".equals(operationName)) {
            if (status >= 500) {
                return "Unable to remove employee login account right now because the authentication service is unavailable.";
            }
            return "Unable to remove employee login account: " + downstreamMessage;
        }

        if ("LeaveClient".equals(clientName) && "getTeamLeaves".equals(operationName)) {
            return "Unable to fetch team leave requests at the moment. Please try again.";
        }

        if ("LeaveClient".equals(clientName) && ("approveLeave".equals(operationName) || "rejectLeave".equals(operationName))) {
            return "Unable to update leave request status right now. Please try again.";
        }

        if (status >= 500) {
            return "A dependent service is currently unavailable. Please try again later.";
        }
        if (status >= 400) {
            return "Request could not be completed because of invalid or conflicting data in a dependent service.";
        }
        return "Dependent service request failed.";
    }
}