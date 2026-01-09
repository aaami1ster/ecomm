package com.aaami.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    @Test
    void calculateDiscount_ShouldReturnZero_ForUserRole() {
        // Given
        String role = "USER";
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(role, amount);

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void calculateDiscount_ShouldReturnTenPercent_ForPremiumUserRole() {
        // Given
        String role = "PREMIUM_USER";
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(role, amount);

        // Then
        assertEquals(new BigDecimal("0.10"), discount);
    }

    @Test
    void calculateDiscount_ShouldReturnFivePercent_ForHighValueOrder() {
        // Given
        String role = "USER";
        BigDecimal amount = new BigDecimal("600.00"); // Above $500 threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(role, amount);

        // Then
        assertEquals(new BigDecimal("0.05"), discount);
    }

    @Test
    void calculateDiscount_ShouldReturnFifteenPercent_ForPremiumUserWithHighValueOrder() {
        // Given
        String role = "PREMIUM_USER";
        BigDecimal amount = new BigDecimal("600.00"); // Above $500 threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(role, amount);

        // Then
        assertEquals(new BigDecimal("0.15"), discount); // 10% + 5%
    }

    @Test
    void calculateDiscount_ShouldCapAtOneHundredPercent() {
        // Given
        String role = "PREMIUM_USER";
        BigDecimal amount = new BigDecimal("10000.00"); // Very high value

        // When
        BigDecimal discount = discountService.calculateDiscount(role, amount);

        // Then
        assertTrue(discount.compareTo(new BigDecimal("1.00")) <= 0);
    }

    @Test
    void applyDiscount_ShouldCalculateCorrectDiscount() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal rate = new BigDecimal("0.10"); // 10%

        // When
        BigDecimal discount = discountService.applyDiscount(amount, rate);

        // Then
        assertEquals(new BigDecimal("10.00"), discount);
    }

    @Test
    void applyDiscount_ShouldReturnZero_WhenRateIsZero() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal rate = BigDecimal.ZERO;

        // When
        BigDecimal discount = discountService.applyDiscount(amount, rate);

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void applyDiscount_ShouldRoundToTwoDecimals() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal rate = new BigDecimal("0.333"); // 33.3%

        // When
        BigDecimal discount = discountService.applyDiscount(amount, rate);

        // Then
        assertEquals(2, discount.scale());
    }
}

