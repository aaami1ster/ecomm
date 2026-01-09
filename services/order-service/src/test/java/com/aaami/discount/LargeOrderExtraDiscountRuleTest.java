package com.aaami.discount;

import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LargeOrderExtraDiscountRule Tests")
class LargeOrderExtraDiscountRuleTest {

    private LargeOrderExtraDiscountRule rule;

    @BeforeEach
    void setUp() {
        rule = new LargeOrderExtraDiscountRule();
    }

    @Test
    @DisplayName("Should be applicable for order above $500 threshold")
    void isApplicable_ShouldReturnTrue_ForOrderAboveThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("600.00");
        UserRole role = UserRole.USER;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertTrue(applicable);
    }

    @Test
    @DisplayName("Should not be applicable for order at $500 threshold")
    void isApplicable_ShouldReturnFalse_ForOrderAtThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("500.00");
        UserRole role = UserRole.USER;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertFalse(applicable);
    }

    @Test
    @DisplayName("Should not be applicable for order below $500 threshold")
    void isApplicable_ShouldReturnFalse_ForOrderBelowThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("499.99");
        UserRole role = UserRole.USER;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertFalse(applicable);
    }

    @Test
    @DisplayName("Should be applicable regardless of user role")
    void isApplicable_ShouldReturnTrue_ForAnyRoleAboveThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("600.00");

        // When & Then
        assertTrue(rule.isApplicable(orderSubtotal, UserRole.USER));
        assertTrue(rule.isApplicable(orderSubtotal, UserRole.PREMIUM_USER));
        assertTrue(rule.isApplicable(orderSubtotal, UserRole.ADMIN));
    }

    @Test
    @DisplayName("Should calculate 5% discount for order above threshold")
    void calculateDiscount_ShouldReturnFivePercent_ForOrderAboveThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("600.00");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("30.00"); // 5% of 600
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return zero for order at threshold")
    void calculateDiscount_ShouldReturnZero_ForOrderAtThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("500.00");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should return zero for order below threshold")
    void calculateDiscount_ShouldReturnZero_ForOrderBelowThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("400.00");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should handle order just above threshold")
    void calculateDiscount_ShouldHandleOrderJustAboveThreshold() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("500.01");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("25.00"); // 5% of 500.01
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should handle very large orders")
    void calculateDiscount_ShouldHandleVeryLargeOrders() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("10000.00");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("500.00"); // 5% of 10000
        assertEquals(expectedDiscount, discount);
    }
}

