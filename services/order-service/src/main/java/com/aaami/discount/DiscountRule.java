package com.aaami.order.service;

import com.aaami.order.domain.Order;
import com.aaami.shared.dto.UserRole;

import java.math.BigDecimal;

public interface DiscountRule {
    /**
     * @return discount amount in money (not percentage), must be >= 0
     */
    BigDecimal calculateDiscount(Order order, UserRole userRole);
    boolean isApplicable(Order order, UserRole userRole);

    default String name() {
        return this.getClass().getSimpleName();
    }
}
