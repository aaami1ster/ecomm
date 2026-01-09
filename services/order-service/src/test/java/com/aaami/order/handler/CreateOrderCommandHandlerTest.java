package com.aaami.order.handler;

import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.OrderItemCommand;
import com.aaami.order.client.ProductServiceClient;
import com.aaami.order.client.UserServiceClient;
import com.aaami.order.domain.Order;
import com.aaami.order.mapper.OrderMapper;
import com.aaami.order.repository.OrderRepository;
import com.aaami.order.service.IdempotencyService;
import com.aaami.order.service.OrderEventProducer;
import com.aaami.discount.DiscountService;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.ProductDto;
import com.aaami.shared.dto.UserDto;
import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private DiscountService discountService;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private OrderEventProducer eventProducer;

    @InjectMocks
    private CreateOrderCommandHandler handler;

    private CreateOrderCommand command;
    private ProductDto productDto;
    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        command = new CreateOrderCommand();
        command.setUserId(1L);
        
        OrderItemCommand itemCommand = new OrderItemCommand();
        itemCommand.setProductId(1L);
        itemCommand.setQuantity(2);
        command.setItems(List.of(itemCommand));

        productDto = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("50.00"))
                .quantity(10)
                .build();

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .build();

        orderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .build();
    }

    @Test
    void handle_ShouldCreateOrder_WhenProductExistsAndStockIsSufficient() {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .role(UserRole.USER)
                .build();
        
        lenient().when(idempotencyService.getCachedOrder(anyString())).thenReturn(null);
        when(userServiceClient.getUser(anyLong())).thenReturn(userDto);
        when(productServiceClient.getProduct(anyLong())).thenReturn(productDto);
        when(discountService.calculateDiscount(any(BigDecimal.class), any(UserRole.class))).thenReturn(BigDecimal.ZERO);
        when(productServiceClient.decreaseProductQuantity(anyLong(), any())).thenReturn(productDto);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);
        doNothing().when(eventProducer).publishOrderCreated(any(OrderDto.class));

        // When
        OrderDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        verify(userServiceClient).getUser(1L);
        verify(productServiceClient).getProduct(1L);
        verify(productServiceClient).decreaseProductQuantity(1L, 2);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    void handle_ShouldReturnCachedOrder_WhenIdempotencyKeyExists() {
        // Given
        String idempotencyKey = "test-key-123";
        command.setIdempotencyKey(idempotencyKey);
        
        OrderDto cachedOrderDto = OrderDto.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .build();
        
        when(idempotencyService.getCachedOrder(idempotencyKey)).thenReturn(cachedOrderDto);

        // When
        OrderDto result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(idempotencyService).getCachedOrder(idempotencyKey);
        verify(userServiceClient, never()).getUser(anyLong());
        verify(productServiceClient, never()).getProduct(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void handle_ShouldCacheOrder_WhenIdempotencyKeyProvided() {
        // Given
        String idempotencyKey = "test-key-456";
        command.setIdempotencyKey(idempotencyKey);
        
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .role(UserRole.USER)
                .build();
        
        when(idempotencyService.getCachedOrder(idempotencyKey)).thenReturn(null);
        when(userServiceClient.getUser(anyLong())).thenReturn(userDto);
        when(productServiceClient.getProduct(anyLong())).thenReturn(productDto);
        when(discountService.calculateDiscount(any(BigDecimal.class), any(UserRole.class))).thenReturn(BigDecimal.ZERO);
        when(productServiceClient.decreaseProductQuantity(anyLong(), any())).thenReturn(productDto);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);
        doNothing().when(eventProducer).publishOrderCreated(any(OrderDto.class));

        // When
        handler.handle(command);

        // Then
        verify(idempotencyService).cacheOrder(idempotencyKey, orderDto);
    }

    @Test
    void handle_ShouldThrowIllegalStateException_WhenProductServiceFails() {
        // Given - HttpClientErrorException extends RestClientException, so it's caught by the RestClientException handler
        // which throws IllegalStateException (not IllegalArgumentException from NotFound handler)
        lenient().when(idempotencyService.getCachedOrder(anyString())).thenReturn(null);
        when(productServiceClient.getProduct(anyLong()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Product not found"));

        // When & Then
        // The handler catches RestClientException (parent of HttpClientErrorException) and throws IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("Unable to fetch product details"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void handle_ShouldThrowIllegalStateException_WhenRestClientExceptionOccurs() {
        // Given
        lenient().when(idempotencyService.getCachedOrder(anyString())).thenReturn(null);
        when(productServiceClient.getProduct(anyLong()))
                .thenThrow(new RestClientException("Service unavailable"));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("Unable to fetch product details"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void handle_ShouldThrowException_WhenInsufficientStock() {
        // Given
        productDto.setQuantity(1); // Less than requested
        lenient().when(idempotencyService.getCachedOrder(anyString())).thenReturn(null);
        when(productServiceClient.getProduct(anyLong())).thenReturn(productDto);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(command));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void handle_ShouldCalculateOrderTotalCorrectly() {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .role(UserRole.USER)
                .build();
        
        lenient().when(idempotencyService.getCachedOrder(anyString())).thenReturn(null);
        when(userServiceClient.getUser(anyLong())).thenReturn(userDto);
        when(productServiceClient.getProduct(anyLong())).thenReturn(productDto);
        when(discountService.calculateDiscount(any(BigDecimal.class), any(UserRole.class))).thenReturn(BigDecimal.ZERO);
        when(productServiceClient.decreaseProductQuantity(anyLong(), any())).thenReturn(productDto);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);
        doNothing().when(eventProducer).publishOrderCreated(any(OrderDto.class));

        // When
        handler.handle(command);

        // Then
        verify(orderRepository).save(argThat(o -> {
            BigDecimal expectedTotal = new BigDecimal("50.00").multiply(new BigDecimal("2"));
            return o.getOrderTotal().compareTo(expectedTotal) == 0 && o.getStatus() == OrderStatus.CONFIRMED;
        }));
    }
}

