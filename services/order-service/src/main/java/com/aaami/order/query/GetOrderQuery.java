package com.aaami.order.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.OrderDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderQuery implements Query<OrderDto> {
    @NotNull(message = "Order ID is required")
    private Long id;
}

