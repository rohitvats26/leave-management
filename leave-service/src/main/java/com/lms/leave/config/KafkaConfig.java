package com.lms.leave.config;

import com.lms.leave.messaging.LeaveEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_LEAVE_APPLIED  = "leave.applied";
    public static final String TOPIC_LEAVE_APPROVED = "leave.approved";
    public static final String TOPIC_LEAVE_REJECTED = "leave.rejected";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, LeaveEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, LeaveEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Auto-create topics with 1 partition, replication-factor 1 (single-broker dev setup)
    @Bean public NewTopic topicApplied()  { return TopicBuilder.name(TOPIC_LEAVE_APPLIED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic topicApproved() { return TopicBuilder.name(TOPIC_LEAVE_APPROVED).partitions(1).replicas(1).build(); }
    @Bean public NewTopic topicRejected() { return TopicBuilder.name(TOPIC_LEAVE_REJECTED).partitions(1).replicas(1).build(); }
}
