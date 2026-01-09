package com.aaami.discount;

import org.springframework.core.annotation.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(20)
public class PremiumUserDiscountRule implements DiscountRule {
    private static final BigDecimal PREMIUM_USER_DISCOUNT = new BigDecimal("0.10"); // 10%

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        if (isApplicable(orderSubtotal, userRole)) {
            return orderSubtotal.multiply(PREMIUM_USER_DISCOUNT)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }

    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        return userRole == UserRole.PREMIUM_USER;
    }
}
