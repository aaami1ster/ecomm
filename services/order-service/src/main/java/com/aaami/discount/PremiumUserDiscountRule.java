package com.aaami.order.service;

import com.aaami.order.domain.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PremiumUserDiscountRule implements DiscountRule {
    private static final BigDecimal PREMIUM_USER_DISCOUNT = new BigDecimal("0.10"); // 10%

    @Override
    public BigDecimal calculateDiscount(Order order, UserRole userRole) {
        if (isApplicable(order, userRole)) {
            return order.getOrderTotal().multiply(PREMIUM_USER_DISCOUNT);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(Order order, UserRole userRole) {
        return userRole.equals(userRole);
    }
}
