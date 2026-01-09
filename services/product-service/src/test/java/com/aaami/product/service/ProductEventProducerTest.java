package com.aaami.product.service;

import com.aaami.shared.dto.ProductDto;
import com.aaami.shared.event.InventoryDecreasedEvent;
import com.aaami.shared.event.ProductCreatedEvent;
import com.aaami.shared.event.ProductDeletedEvent;
import com.aaami.shared.event.ProductUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductEventProducer Tests")
class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ProductEventProducer eventProducer;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventProducer, "productCreatedTopic", "product-created");
        ReflectionTestUtils.setField(eventProducer, "productUpdatedTopic", "product-updated");
        ReflectionTestUtils.setField(eventProducer, "productDeletedTopic", "product-deleted");
        ReflectionTestUtils.setField(eventProducer, "inventoryDecreasedTopic", "inventory-decreased");

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should publish ProductCreatedEvent successfully")
    void publishProductCreated_ShouldSendEventToKafka() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishProductCreated(productDto);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        
        assertEquals("product-created", topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof ProductCreatedEvent);
        ProductCreatedEvent event = (ProductCreatedEvent) eventCaptor.getValue();
        assertEquals(productDto, event.getProduct());
    }

    @Test
    @DisplayName("Should publish ProductUpdatedEvent successfully")
    void publishProductUpdated_ShouldSendEventToKafka() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishProductUpdated(productDto);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());
        
        assertEquals("product-updated", topicCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof ProductUpdatedEvent);
        ProductUpdatedEvent event = (ProductUpdatedEvent) eventCaptor.getValue();
        assertEquals(productDto, event.getProduct());
    }

    @Test
    @DisplayName("Should publish ProductDeletedEvent successfully")
    void publishProductDeleted_ShouldSendEventToKafka() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishProductDeleted(productDto);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());
        
        assertEquals("product-deleted", topicCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof ProductDeletedEvent);
        ProductDeletedEvent event = (ProductDeletedEvent) eventCaptor.getValue();
        assertEquals(productDto, event.getProduct());
    }

    @Test
    @DisplayName("Should publish InventoryDecreasedEvent successfully")
    void publishInventoryDecreased_ShouldSendEventToKafka() {
        // Given
        Integer quantityDecreased = 5;
        Integer remainingQuantity = 5;
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When
        eventProducer.publishInventoryDecreased(productDto, quantityDecreased, remainingQuantity);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), eventCaptor.capture());
        
        assertEquals("inventory-decreased", topicCaptor.getValue());
        assertTrue(eventCaptor.getValue() instanceof InventoryDecreasedEvent);
        InventoryDecreasedEvent event = (InventoryDecreasedEvent) eventCaptor.getValue();
        assertEquals(productDto, event.getProduct());
        assertEquals(quantityDecreased, event.getQuantityDecreased());
        assertEquals(remainingQuantity, event.getRemainingQuantity());
    }

    @Test
    @DisplayName("Should handle Kafka send failure gracefully")
    void publishProductCreated_ShouldHandleKafkaFailure() {
        // Given
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // When & Then - should not throw
        assertDoesNotThrow(() -> eventProducer.publishProductCreated(productDto));
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }
}

