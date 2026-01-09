package com.aaami.discount;

import org.springframework.core.annotation.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(10)
public class LargeOrderExtraDiscountRule implements DiscountRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal FIVE_PERCENT = new BigDecimal("0.05");

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        if (isApplicable(orderSubtotal, userRole)) {
            return orderSubtotal.multiply(FIVE_PERCENT)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }

    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        return orderSubtotal.compareTo(THRESHOLD) > 0;
    }
}
