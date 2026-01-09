package com.aaami.gateway.controller;

import com.aaami.gateway.client.OrderServiceClient;
import com.aaami.shared.command.CreateOrderCommand;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderGatewayController Tests")
class OrderGatewayControllerTest {

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private OrderGatewayController controller;

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
    void createOrder_ShouldReturnCreated() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand();
        when(orderServiceClient.createOrder(any(CreateOrderCommand.class))).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = controller.createOrder(command);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(orderServiceClient).createOrder(any(CreateOrderCommand.class));
    }

    @Test
    @DisplayName("Should get order by ID")
    void getOrder_ShouldReturnOrder() {
        // Given
        when(orderServiceClient.getOrder(1L)).thenReturn(orderDto);

        // When
        ResponseEntity<OrderDto> response = controller.getOrder(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderServiceClient).getOrder(1L);
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
        when(orderServiceClient.getAllOrders(any(), any(), any(), any(), any(), any()))
                .thenReturn(paginatedResponse);

        // When
        ResponseEntity<PaginatedResponse<OrderDto>> response = controller.getAllOrders(
                null, null, 0, 20, null, "desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(orderServiceClient).getAllOrders(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should get user orders")
    void getUserOrders_ShouldReturnListOfOrders() {
        // Given
        when(orderServiceClient.getUserOrders(1L)).thenReturn(List.of(orderDto));

        // When
        ResponseEntity<List<OrderDto>> response = controller.getUserOrders(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(orderServiceClient).getUserOrders(1L);
    }
}

