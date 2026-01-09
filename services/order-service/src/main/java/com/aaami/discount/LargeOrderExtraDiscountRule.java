package com.aaami.order.service;

import com.aaami.order.domain.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LargeOrderExtraDiscountRule implements DiscountRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal FIVE_PERCENT = new BigDecimal("0.05");

    @Override
    public BigDecimal calculateDiscount(Order order, UserRole userRole) {
        if (isApplicable(order, userRole)) {
            return order.getOrderTotal().multiply(FIVE_PERCENT);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(Order order, UserRole userRole) {
        return order.getOrderTotal().compareTo(THRESHOLD) > 0;
    }
}
