package com.aaami.shared.command;

import com.aaami.cqrs.Command;
import com.aaami.shared.dto.OrderDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand implements Command<OrderDto> {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemCommand> items;
    
    /**
     * Idempotency key to prevent duplicate order creation.
     * If provided, the same order will be returned for duplicate requests with the same key.
     */
    private String idempotencyKey;
}

