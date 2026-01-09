package com.aaami.order.mapper;

import com.aaami.order.domain.Order;
import com.aaami.order.domain.OrderItem;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderItemDto;
import com.aaami.shared.dto.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    @DisplayName("Should map Order to OrderDto")
    void toDto_ShouldMapOrderToDto() {
        // Given
        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .build();

        // When
        OrderDto dto = mapper.toDto(order);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals(OrderStatus.PENDING, dto.getStatus());
        assertEquals(new BigDecimal("100.00"), dto.getOrderTotal());
        assertNotNull(dto.getItems());
    }

    @Test
    @DisplayName("Should map Order with items to OrderDto")
    void toDto_ShouldMapOrderWithItems_ToDto() {
        // Given
        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("100.00"))
                .discountApplied(BigDecimal.ZERO)
                .build();

        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .items(List.of(item))
                .build();
        item.setOrder(order);

        // When
        OrderDto dto = mapper.toDto(order);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getItems().size());
        assertEquals(1L, dto.getItems().get(0).getProductId());
        assertEquals(2, dto.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should return null when order is null")
    void toDto_ShouldReturnNull_WhenOrderIsNull() {
        // When
        OrderDto dto = mapper.toDto(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Should map OrderItem to OrderItemDto")
    void toItemDto_ShouldMapOrderItemToDto() {
        // Given
        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("100.00"))
                .discountApplied(new BigDecimal("10.00"))
                .build();

        // When
        OrderItemDto dto = mapper.toItemDto(item);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getProductId());
        assertEquals(2, dto.getQuantity());
        assertEquals(new BigDecimal("50.00"), dto.getUnitPrice());
        assertEquals(new BigDecimal("100.00"), dto.getTotalPrice());
        assertEquals(new BigDecimal("10.00"), dto.getDiscountApplied());
    }

    @Test
    @DisplayName("Should return null when OrderItem is null")
    void toItemDto_ShouldReturnNull_WhenItemIsNull() {
        // When
        OrderItemDto dto = mapper.toItemDto(null);

        // Then
        assertNull(dto);
    }
}

