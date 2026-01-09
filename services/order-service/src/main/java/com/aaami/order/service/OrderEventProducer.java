package com.aaami.order.service;

import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.event.OrderCreatedEvent;
import com.aaami.shared.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing order-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-created:order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.order-status-changed:order-status-changed}")
    private String orderStatusChangedTopic;

    public void publishOrderCreated(OrderDto order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        sendEvent(orderCreatedTopic, order.getId().toString(), event);
        log.info("Published OrderCreatedEvent for order ID: {}", order.getId());
    }

    public void publishOrderStatusChanged(OrderDto order, OrderStatus previousStatus, OrderStatus newStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order, previousStatus, newStatus);
        sendEvent(orderStatusChangedTopic, order.getId().toString(), event);
        log.info("Published OrderStatusChangedEvent for order ID: {}, status: {} -> {}", 
            order.getId(), previousStatus, newStatus);
    }

    private void sendEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Successfully sent event to topic {} with offset {}", topic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event to topic {}", topic, ex);
            }
        });
    }
}

