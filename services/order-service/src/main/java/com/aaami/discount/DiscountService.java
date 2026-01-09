package com.aaami.discount;

import com.aaami.order.domain.Order;
import com.aaami.shared.dto.UserRole;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DiscountService {

    private final List<DiscountRule> rules;

    public DiscountService(List<DiscountRule> rules) {
        this.rules = rules; // Spring injects in @Order
    }

//    private static final BigDecimal PREMIUM_USER_DISCOUNT = new BigDecimal("0.10"); // 10%
//    private static final BigDecimal HIGH_VALUE_ORDER_DISCOUNT = new BigDecimal("0.05"); // 5%
//    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("500.00");

    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        var totalDiscount = rules.stream()
                .filter(strategy -> strategy.isApplicable(orderSubtotal, userRole))
                .map(strategy -> strategy.calculateDiscount(orderSubtotal, userRole))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Guardrail: never discount more than order total
        if (totalDiscount.compareTo(orderSubtotal) > 0) {
            totalDiscount = orderSubtotal;
        }
        // money rounding (2 decimals)
        totalDiscount = totalDiscount.setScale(2, RoundingMode.HALF_UP);

        return totalDiscount;

    }

    private static BigDecimal safeMoney(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        if (v.signum() < 0) throw new IllegalArgumentException("Discount must be >= 0");
        return v;
    }

}

