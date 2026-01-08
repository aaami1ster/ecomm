package com.aaami.gateway.controller;

import com.aaami.gateway.client.OrderServiceClient;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderGatewayController {
    
    private final OrderServiceClient orderServiceClient;
    
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        OrderDto order = orderServiceClient.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderDto>> getAllOrders(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "desc") String sortDirection) {
        PaginatedResponse<OrderDto> response = orderServiceClient.getAllOrders(userId, status, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("id") Long id) {
        OrderDto order = orderServiceClient.getOrder(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable("userId") Long userId) {
        List<OrderDto> orders = orderServiceClient.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
    
    // @PutMapping("/{id}")
    // public ResponseEntity<OrderDto> updateOrder(
    //         @PathVariable("id") Long id,
    //         @RequestBody UpdateOrderCommand command) {
    //     command.setId(id);
    //     OrderDto order = orderServiceClient.updateOrder(id, command);
    //     return ResponseEntity.ok(order);
    // }
    
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
    //     orderServiceClient.deleteOrder(id);
    //     return ResponseEntity.noContent().build();
    // }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(@PathVariable("id") Long id) {
        UpdateOrderCommand command = new UpdateOrderCommand();
        command.setId(id);
        command.setStatus(OrderStatus.CONFIRMED);
        OrderDto order = orderServiceClient.updateOrder(id, command);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable("id") Long id) {
        UpdateOrderCommand command = new UpdateOrderCommand();
        command.setId(id);
        command.setStatus(OrderStatus.CANCELLED);
        OrderDto order = orderServiceClient.updateOrder(id, command);
        return ResponseEntity.ok(order);
    }
}

