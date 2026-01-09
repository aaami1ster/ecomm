package com.aaami.discount;

import com.aaami.shared.dto.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscountService Tests")
class DiscountServiceTest {

    private DiscountService discountService;
    private List<DiscountRule> rules;

    @BeforeEach
    void setUp() {
        rules = new ArrayList<>();
        rules.add(new LargeOrderExtraDiscountRule());
        rules.add(new PremiumUserDiscountRule());
        discountService = new DiscountService(rules);
    }

    @Test
    @DisplayName("Should return zero discount for USER role with order below threshold")
    void calculateDiscount_ShouldReturnZero_ForUserRoleBelowThreshold() {
        // Given
        UserRole role = UserRole.USER;
        BigDecimal orderSubtotal = new BigDecimal("100.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should return 5% discount for USER role with order above $500 threshold")
    void calculateDiscount_ShouldReturnFivePercent_ForUserRoleAboveThreshold() {
        // Given
        UserRole role = UserRole.USER;
        BigDecimal orderSubtotal = new BigDecimal("600.00"); // Above $500 threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("30.00"); // 5% of 600
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return 10% discount for PREMIUM_USER role with order below threshold")
    void calculateDiscount_ShouldReturnTenPercent_ForPremiumUserRoleBelowThreshold() {
        // Given
        UserRole role = UserRole.PREMIUM_USER;
        BigDecimal orderSubtotal = new BigDecimal("100.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("10.00"); // 10% of 100
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return 15% discount (10% + 5%) for PREMIUM_USER role with order above threshold")
    void calculateDiscount_ShouldReturnFifteenPercent_ForPremiumUserRoleAboveThreshold() {
        // Given
        UserRole role = UserRole.PREMIUM_USER;
        BigDecimal orderSubtotal = new BigDecimal("600.00"); // Above $500 threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("90.00"); // 10% (60) + 5% (30) = 90
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return 10% discount for ADMIN role (same as PREMIUM_USER)")
    void calculateDiscount_ShouldReturnTenPercent_ForAdminRole() {
        // Given
        UserRole role = UserRole.ADMIN;
        BigDecimal orderSubtotal = new BigDecimal("100.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("10.00"); // 10% of 100
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should return 15% discount for ADMIN role with order above threshold")
    void calculateDiscount_ShouldReturnFifteenPercent_ForAdminRoleAboveThreshold() {
        // Given
        UserRole role = UserRole.ADMIN;
        BigDecimal orderSubtotal = new BigDecimal("600.00");

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("90.00"); // 10% + 5%
        assertEquals(expectedDiscount, discount);
    }

    @Test
    @DisplayName("Should cap discount at order total (guardrail)")
    void calculateDiscount_ShouldCapAtOrderTotal() {
        // Given
        UserRole role = UserRole.PREMIUM_USER;
        BigDecimal orderSubtotal = new BigDecimal("10.00"); // Small order

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        // 10% of 10 = 1.00, should not exceed order total
        assertTrue(discount.compareTo(orderSubtotal) <= 0);
        assertEquals(new BigDecimal("1.00"), discount);
    }

    @Test
    @DisplayName("Should round discount to 2 decimal places")
    void calculateDiscount_ShouldRoundToTwoDecimals() {
        // Given
        UserRole role = UserRole.PREMIUM_USER;
        BigDecimal orderSubtotal = new BigDecimal("33.33");

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(2, discount.scale());
        assertEquals(new BigDecimal("3.33"), discount);
    }

    @Test
    @DisplayName("Should return zero for zero order subtotal")
    void calculateDiscount_ShouldReturnZero_ForZeroSubtotal() {
        // Given
        UserRole role = UserRole.PREMIUM_USER;
        BigDecimal orderSubtotal = BigDecimal.ZERO;

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should handle order exactly at threshold")
    void calculateDiscount_ShouldHandleOrderAtThreshold() {
        // Given
        UserRole role = UserRole.USER;
        BigDecimal orderSubtotal = new BigDecimal("500.00"); // Exactly at threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        // Should not apply large order discount (threshold is > 500, not >=)
        assertEquals(BigDecimal.ZERO.setScale(2), discount);
    }

    @Test
    @DisplayName("Should handle order just above threshold")
    void calculateDiscount_ShouldHandleOrderJustAboveThreshold() {
        // Given
        UserRole role = UserRole.USER;
        BigDecimal orderSubtotal = new BigDecimal("500.01"); // Just above threshold

        // When
        BigDecimal discount = discountService.calculateDiscount(orderSubtotal, role);

        // Then
        BigDecimal expectedDiscount = new BigDecimal("25.00"); // 5% of 500.01, rounded
        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0);
    }
}

