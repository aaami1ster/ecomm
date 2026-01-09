package com.aaami.order.service;

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

    public BigDecimal calculateDiscount(Order order, UserRole userRole) {
        var orderTotal = order.getOrderTotal();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (var rule : rules) {
            BigDecimal d = safeMoney(rule.calculateDiscount(order, userRole));
            if (d.signum() > 0) {
                totalDiscount = totalDiscount.add(d);
            }
        }

        // Guardrail: never discount more than order total
        if (totalDiscount.compareTo(orderTotal) > 0) {
            totalDiscount = orderTotal;
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

