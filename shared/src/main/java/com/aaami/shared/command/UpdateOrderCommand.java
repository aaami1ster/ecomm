package com.aaami.shared.command;

import com.aaami.cqrs.Command;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderCommand implements Command<OrderDto> {
    @NotNull(message = "Order ID is required")
    private Long id;
    
    private OrderStatus status;
}

