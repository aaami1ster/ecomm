package com.aaami.order.query;

import com.aaami.cqrs.Query;
import com.aaami.order.dto.OrderDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserOrdersQuery implements Query<List<OrderDto>> {
    @NotNull(message = "User ID is required")
    private Long userId;
}

