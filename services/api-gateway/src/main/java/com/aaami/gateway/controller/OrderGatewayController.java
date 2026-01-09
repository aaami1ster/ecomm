package com.aaami.gateway.controller;

import com.aaami.gateway.client.OrderServiceClient;
import com.aaami.shared.command.CreateOrderCommand;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints. Available to all authenticated users (USER, PREMIUM_USER, ADMIN).")
public class OrderGatewayController {
    
    private final OrderServiceClient orderServiceClient;
    
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the specified items. Product inventory is automatically decreased upon successful order creation. Orders are created with CONFIRMED status. Supports idempotency key to prevent duplicate orders. Available to all authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or insufficient stock",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate request detected (same idempotency key used)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderCommand command) {
        OrderDto order = orderServiceClient.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @Operation(
            summary = "Get all orders",
            description = "Retrieves a paginated list of orders with optional filtering by userId and status. Available to all authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderDto>> getAllOrders(
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(value = "userId", required = false) Long userId,
            @Parameter(description = "Filter by order status", example = "PENDING", schema = @Schema(implementation = OrderStatus.class))
            @RequestParam(value = "status", required = false) OrderStatus status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort field (e.g., createdAt, orderTotal)", example = "createdAt")
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(value = "sortDirection", required = false, defaultValue = "desc") String sortDirection) {
        PaginatedResponse<OrderDto> response = orderServiceClient.getAllOrders(userId, status, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order by its ID. Available to all authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(description = "Order ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        OrderDto order = orderServiceClient.getOrder(id);
        return ResponseEntity.ok(order);
    }
    
    @Operation(
            summary = "Get orders by user ID",
            description = "Retrieves all orders for a specific user. Available to all authenticated users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token",
                    content = @Content
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable("userId") Long userId) {
        List<OrderDto> orders = orderServiceClient.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
    
}

