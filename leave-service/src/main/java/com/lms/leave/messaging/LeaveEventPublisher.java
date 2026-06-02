package com.lms.leave.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void publish(String topic, LeaveEvent event) {
        // Use leaveRequestId as partition key so same request always hits same partition
        String key = event.getLeaveRequestId().toString();

        CompletableFuture<SendResult<Object, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[KAFKA] Failed to publish {} to topic={} key={}: {}",
                        event.getEventType(), topic, key, ex.getMessage());
            } else {
                log.info("[KAFKA] Published {} → topic={} partition={} offset={}",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
