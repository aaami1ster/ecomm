package com.aaami.discount;

import org.springframework.core.annotation.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(20)
public class AdminDiscountRule implements DiscountRule {
    private static final BigDecimal ADMIN_DISCOUNT = new BigDecimal("0.15"); // 15%

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        if (isApplicable(orderSubtotal, userRole)) {
            return orderSubtotal.multiply(ADMIN_DISCOUNT)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }

    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        return userRole == UserRole.ADMIN;
    }
}

