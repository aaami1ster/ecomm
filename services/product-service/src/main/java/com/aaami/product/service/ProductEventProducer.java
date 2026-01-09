package com.aaami.product.service;

import com.aaami.shared.dto.ProductDto;
import com.aaami.shared.event.InventoryDecreasedEvent;
import com.aaami.shared.event.ProductCreatedEvent;
import com.aaami.shared.event.ProductDeletedEvent;
import com.aaami.shared.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing product-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-created:product-created}")
    private String productCreatedTopic;

    @Value("${kafka.topics.product-updated:product-updated}")
    private String productUpdatedTopic;

    @Value("${kafka.topics.product-deleted:product-deleted}")
    private String productDeletedTopic;

    @Value("${kafka.topics.inventory-decreased:inventory-decreased}")
    private String inventoryDecreasedTopic;

    public void publishProductCreated(ProductDto product) {
        ProductCreatedEvent event = new ProductCreatedEvent(product);
        sendEvent(productCreatedTopic, product.getId().toString(), event);
        log.info("Published ProductCreatedEvent for product ID: {}", product.getId());
    }

    public void publishProductUpdated(ProductDto product) {
        ProductUpdatedEvent event = new ProductUpdatedEvent(product);
        sendEvent(productUpdatedTopic, product.getId().toString(), event);
        log.info("Published ProductUpdatedEvent for product ID: {}", product.getId());
    }

    public void publishProductDeleted(ProductDto product) {
        ProductDeletedEvent event = new ProductDeletedEvent(product);
        sendEvent(productDeletedTopic, product.getId().toString(), event);
        log.info("Published ProductDeletedEvent for product ID: {}", product.getId());
    }

    public void publishInventoryDecreased(ProductDto product, Integer quantityDecreased, Integer remainingQuantity) {
        InventoryDecreasedEvent event = new InventoryDecreasedEvent(product, quantityDecreased, remainingQuantity);
        sendEvent(inventoryDecreasedTopic, product.getId().toString(), event);
        log.info("Published InventoryDecreasedEvent for product ID: {}, quantity decreased: {}, remaining: {}", 
            product.getId(), quantityDecreased, remainingQuantity);
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

