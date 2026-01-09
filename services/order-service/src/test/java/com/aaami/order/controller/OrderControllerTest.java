package com.aaami.order.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import com.aaami.order.query.GetAllOrdersQuery;
import com.aaami.order.query.GetOrderQuery;
import com.aaami.order.query.GetUserOrdersQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private QueryBus queryBus;

    @InjectMocks
    private OrderController controller;

    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_ShouldReturnCreated_WhenOrderCreated() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand();
        command.setUserId(1L);
        when(commandBus.dispatch(any(CreateOrderCommand.class))).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = controller.createOrder(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(commandBus).dispatch(any(CreateOrderCommand.class));
    }

    @Test
    @DisplayName("Should get order by ID")
    void getOrder_ShouldReturnOrder_WhenOrderExists() {
        // Given
        when(queryBus.dispatch(any(GetOrderQuery.class))).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = controller.getOrder(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(queryBus).dispatch(any(GetOrderQuery.class));
    }

    @Test
    @DisplayName("Should get all orders with pagination")
    void getAllOrders_ShouldReturnPaginatedResponse() {
        // Given
        PaginatedResponse<OrderDto> paginatedResponse = PaginatedResponse.<OrderDto>builder()
                .content(List.of(orderDto))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
        when(queryBus.dispatch(any(GetAllOrdersQuery.class))).thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<OrderDto>> response = controller.getAllOrders(
                null, null, 0, 20, null, "desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(queryBus).dispatch(any(GetAllOrdersQuery.class));
    }

    @Test
    @DisplayName("Should get user orders")
    void getUserOrders_ShouldReturnListOfOrders() {
        // Given
        when(queryBus.dispatch(any(GetUserOrdersQuery.class))).thenReturn(List.of(orderDto));

        // When
        ResponseEntity<List<OrderDto>> response = controller.getUserOrders(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(queryBus).dispatch(any(GetUserOrdersQuery.class));
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteOrder_ShouldReturnNoContent() {
        // Given
        when(commandBus.dispatch(any(DeleteOrderCommand.class))).thenReturn(null);

        // When
        ResponseEntity<Void> response = controller.deleteOrder(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(commandBus).dispatch(any(DeleteOrderCommand.class));
    }
}

