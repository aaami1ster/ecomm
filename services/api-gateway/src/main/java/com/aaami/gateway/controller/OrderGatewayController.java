package com.aaami.gateway.controller;

import com.aaami.gateway.client.OrderServiceClient;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
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
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) OrderStatus status) {
        List<OrderDto> orders = orderServiceClient.getAllOrders(userId, status);
        return ResponseEntity.ok(orders);
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
    
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable("id") Long id,
            @RequestBody UpdateOrderCommand command) {
        command.setId(id);
        OrderDto order = orderServiceClient.updateOrder(id, command);
        return ResponseEntity.ok(order);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        orderServiceClient.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}

