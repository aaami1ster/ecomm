package com.aaami.order.command;

import com.aaami.cqrs.Command;
import com.aaami.order.dto.OrderDto;
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
}

