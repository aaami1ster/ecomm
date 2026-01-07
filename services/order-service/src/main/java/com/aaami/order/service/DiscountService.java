package com.aaami.order.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DiscountService {
    
    private static final BigDecimal PREMIUM_USER_DISCOUNT = new BigDecimal("0.10"); // 10%
    private static final BigDecimal HIGH_VALUE_ORDER_DISCOUNT = new BigDecimal("0.05"); // 5%
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("500.00");
    
    public BigDecimal calculateDiscount(String userRole, BigDecimal orderTotal) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        // Premium user discount: 10%
        if ("PREMIUM_USER".equals(userRole)) {
            totalDiscount = totalDiscount.add(PREMIUM_USER_DISCOUNT);
        }
        
        // High value order discount: 5% for orders > $500
        if (orderTotal.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            totalDiscount = totalDiscount.add(HIGH_VALUE_ORDER_DISCOUNT);
        }
        
        return totalDiscount.min(new BigDecimal("1.00")); // Cap at 100%
    }
    
    public BigDecimal applyDiscount(BigDecimal amount, BigDecimal discountRate) {
        return amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}

