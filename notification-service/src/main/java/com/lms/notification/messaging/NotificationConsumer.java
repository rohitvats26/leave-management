package com.lms.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lms.notification.config.KafkaConfig;
import com.lms.notification.dto.LeaveEvent;
import com.lms.notification.entity.NotificationLog;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository repo;
    private static final Logger CONSOLE_ONLY_LOG = LoggerFactory.getLogger("CONSOLE_ONLY");

    @KafkaListener(topics = KafkaConfig.TOPIC_LEAVE_APPLIED, groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLeaveApplied(@Payload LeaveEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset, Acknowledgment ack) {
        try {
            log.info("{}", kafkaEventLog("LeaveApplied", event, partition, offset));

            // Notify employee
            save(event, event.getEmployeeId(), "EMPLOYEE",
                    String.format("Your %s leave from %s to %s has been submitted — PENDING approval.",
                            event.getLeaveType(), event.getStartDate(), event.getEndDate()));

            // Notify manager
            save(event, event.getManagerId(), "MANAGER",
                    String.format("Employee %s applied for %d days %s leave (%s → %s). Action required.",
                            event.getEmployeeId(), event.getNumberOfDays(),
                            event.getLeaveType(), event.getStartDate(), event.getEndDate()));

            ack.acknowledge(); // commit offset only on success
        } catch (Exception e) {
            log.error("[KAFKA] Error processing LeaveApplied event: {}", e.getMessage(), e);
            // Do NOT ack — message will be reprocessed
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_LEAVE_APPROVED, groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLeaveApproved(
            @Payload LeaveEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("{}", kafkaEventLog("LeaveApproved", event, partition, offset));

            save(event, event.getEmployeeId(), "EMPLOYEE",
                    String.format("✅ Your %s leave from %s to %s has been APPROVED.%s",
                            event.getLeaveType(), event.getStartDate(), event.getEndDate(),
                            event.getComments() != null ? " Comment: " + event.getComments() : ""));

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[KAFKA] Error processing LeaveApproved event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_LEAVE_REJECTED,
            groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLeaveRejected(
            @Payload LeaveEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("{}", kafkaEventLog("LeaveRejected", event, partition, offset));

            save(event, event.getEmployeeId(), "EMPLOYEE",
                    String.format("❌ Your %s leave from %s to %s was REJECTED. Reason: %s",
                            event.getLeaveType(), event.getStartDate(), event.getEndDate(),
                            event.getRejectionReason()));

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[KAFKA] Error processing LeaveRejected event: {}", e.getMessage(), e);
        }
    }

    private void save(LeaveEvent event, UUID recipientId, String type, String message) {
        NotificationLog log_entry = NotificationLog.builder()
                .eventType(event.getEventType())
                .recipientId(recipientId)
                .recipientType(type)
                .message(message)
                .leaveRequestId(event.getLeaveRequestId())
                .build();
        repo.save(log_entry);
        log.info("{}", notificationLog(log_entry));
    }

    private ObjectNode kafkaEventLog(String eventName, LeaveEvent event, int partition, long offset) {
        if (event == null) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode kafkaEvent = mapper.createObjectNode();
        kafkaEvent.put("eventType", eventName);
        kafkaEvent.put("partition", partition);
        kafkaEvent.put("offset", offset);
        kafkaEvent.put("leaveRequestId", safe(event.getLeaveRequestId()));
        kafkaEvent.put("employeeId", safe(event.getEmployeeId()));
        kafkaEvent.put("managerId", safe(event.getManagerId()));
        kafkaEvent.put("leaveType", safe(event.getLeaveType()));
        kafkaEvent.put("startDate", safe(event.getStartDate()));
        kafkaEvent.put("endDate", safe(event.getEndDate()));
        kafkaEvent.put("numberOfDays", safe(event.getNumberOfDays()));
        kafkaEvent.put("comments", safe(event.getComments()));
        kafkaEvent.put("rejectionReason", safe(event.getRejectionReason()));
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.set("kafkaEvent", kafkaEvent);
        printPrettyKafkaEventLog(wrapper);
        return wrapper;
    }

    private void printPrettyKafkaEventLog(ObjectNode kafkaEvent) {
        try {
            CONSOLE_ONLY_LOG.info("KAFKA EVENT ==> {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(kafkaEvent));
        } catch (Exception e) {
            log.error("Failed to serialize Kafka event to JSON", e);
        }
    }

    private ObjectNode notificationLog(NotificationLog notificationLog) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode notification = mapper.createObjectNode();
        notification.put("eventType", safe(notificationLog.getEventType()));
        notification.put("recipientId", safe(notificationLog.getRecipientId()));
        notification.put("recipientType", safe(notificationLog.getRecipientType()));
        notification.put("leaveId", safe(notificationLog.getLeaveRequestId()));
        notification.put("message", safe(notificationLog.getMessage()));
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.set("notification", notification);
        printPrettyNotificationLog(wrapper);
        return wrapper;
    }

    private void printPrettyNotificationLog(ObjectNode notification) {
        try {
            CONSOLE_ONLY_LOG.info("NOTIFICATION LOG ==> {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(notification));
        } catch (Exception e) {
            log.error("Failed to serialize notification log to JSON", e);
        }
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
