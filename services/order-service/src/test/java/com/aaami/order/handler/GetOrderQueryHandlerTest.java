package com.aaami.order.handler;

import com.aaami.order.domain.Order;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetOrderQuery;
import com.aaami.order.repository.OrderRepository;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderQueryHandler Tests")
class GetOrderQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private GetOrderQueryHandler handler;

    private GetOrderQuery query;
    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        query = new GetOrderQuery();
        query.setId(1L);

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();

        orderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should return order DTO when order exists")
    void handle_ShouldReturnOrderDto_WhenOrderExists() {
        // Given
        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        // When
        OrderDto result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        verify(orderRepository).findByIdAndDeletedAtIsNull(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when order does not exist")
    void handle_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
        assertEquals("Order not found with id: 1", exception.getMessage());
        verify(orderRepository).findByIdAndDeletedAtIsNull(1L);
        verify(orderMapper, never()).toDto(any());
    }
}

