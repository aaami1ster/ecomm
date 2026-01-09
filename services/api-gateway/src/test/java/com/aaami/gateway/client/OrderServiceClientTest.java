package com.aaami.gateway.client;

import com.aaami.gateway.config.ServiceProperties;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceClient Tests")
class OrderServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceProperties serviceProperties;

    @InjectMocks
    private OrderServiceClient client;

    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        when(serviceProperties.getOrderServiceUrl()).thenReturn("http://localhost:8082");

        orderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .orderTotal(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_ShouldReturnOrderDto() {
        // Given
        CreateOrderCommand command = new CreateOrderCommand();
        ResponseEntity<OrderDto> response = new ResponseEntity<>(orderDto, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(CreateOrderCommand.class), eq(OrderDto.class)))
                .thenReturn(response);

        // When
        OrderDto result = client.createOrder(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).postForEntity("http://localhost:8082/api/orders", command, OrderDto.class);
    }

    @Test
    @DisplayName("Should get order by ID")
    void getOrder_ShouldReturnOrderDto() {
        // Given
        ResponseEntity<OrderDto> response = new ResponseEntity<>(orderDto, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(OrderDto.class))).thenReturn(response);

        // When
        OrderDto result = client.getOrder(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restTemplate).getForEntity("http://localhost:8082/api/orders/1", OrderDto.class);
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
        ResponseEntity<PaginatedResponse<OrderDto>> response = new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        PaginatedResponse<OrderDto> result = client.getAllOrders(null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should get user orders")
    void getUserOrders_ShouldReturnListOfOrders() {
        // Given
        ResponseEntity<List<OrderDto>> response = new ResponseEntity<>(List.of(orderDto), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        // When
        List<OrderDto> result = client.getUserOrders(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Should update order successfully")
    void updateOrder_ShouldReturnUpdatedOrderDto() {
        // Given
        UpdateOrderCommand command = new UpdateOrderCommand();
        ResponseEntity<OrderDto> response = new ResponseEntity<>(orderDto, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(OrderDto.class)))
                .thenReturn(response);

        // When
        OrderDto result = client.updateOrder(1L, command);

        // Then
        assertNotNull(result);
        verify(restTemplate).exchange(
                eq("http://localhost:8082/api/orders/1"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(OrderDto.class)
        );
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteOrder_ShouldCallDeleteEndpoint() {
        // When
        client.deleteOrder(1L);

        // Then
        verify(restTemplate).delete("http://localhost:8082/api/orders/1");
    }
}

