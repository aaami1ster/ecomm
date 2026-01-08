package com.aaami.order.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllOrdersQuery implements Query<List<OrderDto>> {
    private Long userId;
    private OrderStatus status;
}

