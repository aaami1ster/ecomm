package com.aaami.order.query;

import com.aaami.cqrs.Query;
import com.aaami.shared.dto.OrderDto;
import com.aaami.shared.dto.OrderStatus;
import com.aaami.shared.dto.PaginatedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllOrdersQuery implements Query<PaginatedResponse<OrderDto>> {
    private Long userId;
    private OrderStatus status;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // "asc" or "desc"
}

