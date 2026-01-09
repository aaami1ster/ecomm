package com.aaami.order.handler;

import com.aaami.order.domain.Order;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.query.GetAllOrdersQuery;
import com.aaami.order.repository.OrderRepository;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllOrdersQueryHandler Tests")
class GetAllOrdersQueryHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private GetAllOrdersQueryHandler handler;

    private GetAllOrdersQuery query;
    private Order order1;
    private Order order2;
    private OrderDto orderDto1;
    private OrderDto orderDto2;

    @BeforeEach
    void setUp() {
        query = new GetAllOrdersQuery();

        order1 = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();

        order2 = Order.builder()
                .id(2L)
                .userId(2L)
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
                .userId(2L)
                .status(OrderStatus.CONFIRMED)
                .orderTotal(new BigDecimal("200.00"))
                .build();
    }

    @Test
    @DisplayName("Should return paginated orders with default pagination")
    void handle_ShouldReturnPaginatedOrders_WithDefaultPagination() {
        // Given
        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 20), 2);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);
        when(orderMapper.toDto(order2)).thenReturn(orderDto2);

        // When
        PaginatedResponse<OrderDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by userId when provided")
    void handle_ShouldFilterByUserId_WhenProvided() {
        // Given
        query.setUserId(1L);
        Page<Order> orderPage = new PageImpl<>(List.of(order1), PageRequest.of(0, 20), 1);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);

        // When
        PaginatedResponse<OrderDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserId());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by status when provided")
    void handle_ShouldFilterByStatus_WhenProvided() {
        // Given
        query.setStatus(OrderStatus.PENDING);
        Page<Order> orderPage = new PageImpl<>(List.of(order1), PageRequest.of(0, 20), 1);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderMapper.toDto(order1)).thenReturn(orderDto1);

        // When
        PaginatedResponse<OrderDto> result = handler.handle(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}

