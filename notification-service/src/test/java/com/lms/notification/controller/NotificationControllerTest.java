package com.lms.notification.controller;

import com.lms.notification.dto.ErrorResponse;
import com.lms.notification.entity.NotificationLog;
import com.lms.notification.exception.GlobalExceptionHandler;
import com.lms.notification.exception.ResourceNotFoundException;
import com.lms.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NotificationControllerTest {

    private NotificationControllerTest() {
    }

    public static void main(String[] args) {
        shouldReturnStructuredNotFoundResponse();
        shouldRejectInvalidPagination();
        shouldSupportRestfulNotificationsAlias();
    }

    private static void shouldReturnStructuredNotFoundResponse() {
        NotificationController controller = new NotificationController(repositoryReturning(null));
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UUID notificationId = UUID.randomUUID();

        try {
            controller.getById(notificationId);
            throw new AssertionError("Expected ResourceNotFoundException for missing notification");
        } catch (ResourceNotFoundException ex) {
            ResponseEntity<ErrorResponse> response = handler.notFound(ex, null);
            ErrorResponse body = response.getBody();
            if (response.getStatusCode().value() != 404 || body == null) {
                throw new AssertionError("Expected structured not found response");
            }
            if (!"RESOURCE_NOT_FOUND".equals(body.getCode())) {
                throw new AssertionError("Expected RESOURCE_NOT_FOUND code");
            }
        }
    }

    private static void shouldRejectInvalidPagination() {
        NotificationController controller = new NotificationController(repositoryReturning(null));
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        try {
            controller.myNotifications(UUID.randomUUID().toString(), -1, 10);
            throw new AssertionError("Expected IllegalArgumentException for negative page");
        } catch (IllegalArgumentException ex) {
            ResponseEntity<ErrorResponse> response = handler.badRequest(ex, null);
            ErrorResponse body = response.getBody();
            if (response.getStatusCode().value() != 400 || body == null) {
                throw new AssertionError("Expected structured bad request response");
            }
            if (!"INVALID_REQUEST".equals(body.getCode())) {
                throw new AssertionError("Expected INVALID_REQUEST code");
            }
        }
    }

    private static void shouldSupportRestfulNotificationsAlias() {
        UUID recipientId = UUID.randomUUID();
        NotificationController controller = new NotificationController(repositoryReturning(new NotificationLog()));

        ResponseEntity<?> response = controller.myNotifications(recipientId.toString(), 0, 10);
        if (response.getStatusCode().value() != 200) {
            throw new AssertionError("Expected notifications collection endpoint to return 200");
        }
    }

    private static NotificationRepository repositoryReturning(NotificationLog lookupResult) {
        return (NotificationRepository) Proxy.newProxyInstance(
                NotificationControllerTest.class.getClassLoader(),
                new Class[]{NotificationRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(lookupResult);
                    case "findByRecipientIdOrderByCreatedAtDesc" -> new PageImpl<>(List.of(new NotificationLog()));
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "NotificationRepositoryProxy";
                    default -> null;
                }
        );
    }

}
