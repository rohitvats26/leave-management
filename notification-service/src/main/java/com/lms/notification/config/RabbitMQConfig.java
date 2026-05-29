/*
package com.lms.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.*;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "leave.events";
    public static final String APPLIED_Q = "leave.applied.queue";
    public static final String APPROVED_Q = "leave.approved.queue";
    public static final String REJECTED_Q = "leave.rejected.queue";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue appliedQueue() {
        return new Queue(APPLIED_Q, true);
    }

    @Bean
    Queue approvedQueue() {
        return new Queue(APPROVED_Q, true);
    }

    @Bean
    Queue rejectedQueue() {
        return new Queue(REJECTED_Q, true);
    }

    @Bean
    Binding appliedBinding(Queue appliedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(appliedQueue).to(exchange).with("leave.applied");
    }

    @Bean
    Binding approvedBinding(Queue approvedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(approvedQueue).to(exchange).with("leave.approved");
    }

    @Bean
    Binding rejectedBinding(Queue rejectedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(rejectedQueue).to(exchange).with("leave.rejected");
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(mc);
        return rt;
    }
}
*/
