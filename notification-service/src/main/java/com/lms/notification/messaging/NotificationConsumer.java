package com.lms.notification.messaging;

import com.lms.notification.config.KafkaConfig;
import com.lms.notification.dto.LeaveEvent;
import com.lms.notification.entity.NotificationLog;
import com.lms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @KafkaListener(
            topics = KafkaConfig.TOPIC_LEAVE_APPLIED,
            groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLeaveApplied(
            @Payload LeaveEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("[KAFKA] Consumed LeaveApplied | partition={} offset={} leaveId={}",
                    partition, offset, event.getLeaveRequestId());

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

    @KafkaListener(
            topics = KafkaConfig.TOPIC_LEAVE_APPROVED,
            groupId = KafkaConfig.GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onLeaveApproved(
            @Payload LeaveEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("[KAFKA] Consumed LeaveApproved | partition={} offset={} leaveId={}",
                    partition, offset, event.getLeaveRequestId());

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
            log.info("[KAFKA] Consumed LeaveRejected | partition={} offset={} leaveId={}",
                    partition, offset, event.getLeaveRequestId());

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
        log.info("[NOTIFICATION] To={} ({}): {}", recipientId, type, message);
    }
}
