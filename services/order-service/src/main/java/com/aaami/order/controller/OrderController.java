package com.aaami.order.controller;

import com.aaami.cqrs.CommandBus;
import com.aaami.cqrs.QueryBus;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.command.DeleteOrderCommand;
import com.aaami.shared.command.UpdateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.order.query.GetAllOrdersQuery;
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
    
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) OrderStatus status) {
        GetAllOrdersQuery query = GetAllOrdersQuery.builder()
                .userId(userId)
                .status(status)
                .build();
        List<OrderDto> orders = queryBus.dispatch(query);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("id") Long id) {
        GetOrderQuery query = new GetOrderQuery(id);
        OrderDto order = queryBus.dispatch(query);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable("userId") Long userId) {
        GetUserOrdersQuery query = new GetUserOrdersQuery(userId);
        List<OrderDto> orders = queryBus.dispatch(query);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable("id") Long id,
            @RequestBody UpdateOrderCommand command) {
        command.setId(id);
        OrderDto order = commandBus.dispatch(command);
        return ResponseEntity.ok(order);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
        DeleteOrderCommand command = new DeleteOrderCommand(id);
        commandBus.dispatch(command);
        return ResponseEntity.noContent().build();
    }
}

