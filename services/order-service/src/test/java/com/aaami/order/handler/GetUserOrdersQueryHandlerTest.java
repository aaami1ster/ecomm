package com.aaami.order.handler;

import com.aaami.order.domain.Order;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetUserOrdersQuery;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserOrdersQueryHandler Tests")
class GetUserOrdersQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private GetUserOrdersQueryHandler handler;

    private GetUserOrdersQuery query;
    private Order order1;
    private Order order2;
    private OrderDto orderDto1;
    private OrderDto orderDto2;

    @BeforeEach
    void setUp() {
        query = new GetUserOrdersQuery();
        query.setUserId(1L);

        order1 = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();

        order2 = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .orderTotal(new BigDecimal("200.00"))
                .build();

        orderDto1 = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();

        orderDto2 = OrderDto.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .orderTotal(new BigDecimal("200.00"))
                .build();
    }

    @Test
    @DisplayName("Should return list of order DTOs for user")
    void handle_ShouldReturnOrderDtos_ForUser() {
        // Given
        when(orderRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toDto(order2)).thenReturn(orderDto2);

        // When
        List<OrderDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(orderRepository).findByUserIdAndDeletedAtIsNull(1L);
        verify(orderMapper).toDto(order1);
        verify(orderMapper).toDto(order2);
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void handle_ShouldReturnEmptyList_WhenUserHasNoOrders() {
        // Given
        when(orderRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        // When
        List<OrderDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByUserIdAndDeletedAtIsNull(1L);
        verify(orderMapper, never()).toDto(any());
    }
}

