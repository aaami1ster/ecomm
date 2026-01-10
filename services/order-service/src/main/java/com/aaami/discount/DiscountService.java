package com.aaami.discount;

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

    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        var totalDiscount = rules.stream()
                .filter(rule -> rule.isApplicable(orderSubtotal, userRole))
                .map(rule -> rule.calculateDiscount(orderSubtotal, userRole))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Guardrail: never discount more than order total
        if (totalDiscount.compareTo(orderSubtotal) > 0) {
            totalDiscount = orderSubtotal;
        }
        // money rounding (2 decimals)
        totalDiscount = totalDiscount.setScale(2, RoundingMode.HALF_UP);

        return totalDiscount;
    }
}

