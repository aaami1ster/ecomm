package com.aaami.discount;

import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PremiumUserDiscountRule Tests")
class PremiumUserDiscountRuleTest {

    private PremiumUserDiscountRule rule;

    @BeforeEach
    void setUp() {
        rule = new PremiumUserDiscountRule();
    }

    @Test
    @DisplayName("Should be applicable for PREMIUM_USER role")
    void isApplicable_ShouldReturnTrue_ForPremiumUserRole() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("100.00");
        UserRole role = UserRole.PREMIUM_USER;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertTrue(applicable);
    }

    @Test
    @DisplayName("Should be applicable for ADMIN role")
    void isApplicable_ShouldReturnTrue_ForAdminRole() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("100.00");
        UserRole role = UserRole.ADMIN;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertTrue(applicable);
    }

    @Test
    @DisplayName("Should not be applicable for USER role")
    void isApplicable_ShouldReturnFalse_ForUserRole() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("100.00");
        UserRole role = UserRole.USER;

        // When
        boolean applicable = rule.isApplicable(orderSubtotal, role);

        // Then
        assertFalse(applicable);
    }

    @Test
    @DisplayName("Should calculate 10% discount for PREMIUM_USER")
    void calculateDiscount_ShouldReturnTenPercent_ForPremiumUser() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("100.00");
        UserRole role = UserRole.PREMIUM_USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("10.00");
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should calculate 10% discount for ADMIN")
    void calculateDiscount_ShouldReturnTenPercent_ForAdmin() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("200.00");
        UserRole role = UserRole.ADMIN;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("20.00");
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return zero for USER role")
    void calculateDiscount_ShouldReturnZero_ForUserRole() {
        // Given
        BigDecimal orderSubtotal = new BigDecimal("100.00");
        UserRole role = UserRole.USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should handle zero order subtotal")
    void calculateDiscount_ShouldReturnZero_ForZeroSubtotal() {
        // Given
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        UserRole role = UserRole.PREMIUM_USER;

        // When
        BigDecimal discount = rule.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }
}

