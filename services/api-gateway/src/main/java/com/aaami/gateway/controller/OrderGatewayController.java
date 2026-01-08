package com.aaami.gateway.controller;

import com.aaami.gateway.client.OrderServiceClient;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.dto.OrderDto;
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
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        OrderDto order = orderServiceClient.getOrder(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        List<OrderDto> orders = orderServiceClient.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
}

