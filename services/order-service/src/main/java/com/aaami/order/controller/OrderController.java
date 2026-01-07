package com.aaami.order.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.order.command.CreateOrderCommand;
import com.aaami.order.dto.OrderDto;
import com.aaami.order.query.GetOrderQuery;
import com.aaami.order.query.GetUserOrdersQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        OrderDto order = commandBus.dispatch(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        GetOrderQuery query = new GetOrderQuery(id);
        OrderDto order = queryBus.dispatch(query);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        GetUserOrdersQuery query = new GetUserOrdersQuery(userId);
        List<OrderDto> orders = queryBus.dispatch(query);
        return ResponseEntity.ok(orders);
    }
}

