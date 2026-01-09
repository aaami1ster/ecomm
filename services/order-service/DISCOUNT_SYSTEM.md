# Discount System Documentation

## Overview

The discount system uses the **Chain of Responsibility / Pipeline pattern** to apply multiple discount rules in a flexible and extensible way. This design allows for easy addition of new discount rules without modifying existing code.

## Architecture

### Design Pattern: Chain of Responsibility / Pipeline

The discount system implements a pipeline where multiple discount rules are evaluated and their discounts are combined additively.

### Components

#### 1. DiscountRule Interface

```java
public interface DiscountRule {
    BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole);
    boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole);
    default String name() {
        return this.getClass().getSimpleName();
    }
}
```

**Responsibilities**:
- Defines the contract for all discount rules
- Each rule determines if it's applicable and calculates its discount amount
- Rules return discount amounts (not percentages) in money units

#### 2. DiscountService

```java
@Service
public class DiscountService {
    private final List<DiscountRule> rules;
    
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        // Filters applicable rules
        // Sums discounts from all applicable rules
        // Applies guardrails (discount <= order total)
        // Rounds to 2 decimal places
    }
}
```

**Responsibilities**:
- Orchestrates discount calculation
- Collects all `DiscountRule` beans via Spring dependency injection
- Filters rules based on `isApplicable()` method
- Sums discounts from all applicable rules (additive)
- Applies guardrails:
  - Discount cannot exceed order total
  - Discounts are rounded to 2 decimal places

#### 3. Discount Rules (Implementations)

Rules are implemented as Spring `@Component` beans with `@Order` annotation to control execution order:

##### LargeOrderExtraDiscountRule (`@Order(10)`)
- **Priority**: 10 (executed first)
- **Applicable**: Orders above $500.00
- **Discount**: 5% of order subtotal
- **Applies to**: All user roles (USER, PREMIUM_USER, ADMIN)

##### PremiumUserDiscountRule (`@Order(20)`)
- **Priority**: 20 (executed second)
- **Applicable**: PREMIUM_USER role
- **Discount**: 10% of order subtotal
- **Applies to**: PREMIUM_USER, ADMIN

## Discount Calculation Flow

```
Order Created
    ↓
Get User Role (from User Service)
    ↓
Calculate Order Subtotal
    ↓
DiscountService.calculateDiscount()
    ↓
For each DiscountRule:
    ├─ Check isApplicable()
    ├─ If applicable: calculateDiscount()
    └─ Add to total discount
    ↓
Apply Guardrails:
    ├─ Cap at order total
    └─ Round to 2 decimals
    ↓
Return Total Discount
```

## Discount Examples

| User Role    | Order Amount | Large Order Rule | Premium Rule    | Total Discount |
| ------------ | ------------ | ---------------- | --------------- | -------------- |
| USER         | $100         | No (below $500)  | No              | $0             |
| USER         | $600         | Yes (5% = $30)   | No              | $30            |
| PREMIUM_USER | $100         | No               | Yes (10% = $10) | $10            |
| PREMIUM_USER | $600         | Yes (5% = $30)   | Yes (10% = $60) | $90 (15%)      |

## Adding a New Discount Rule

To add a new discount rule:

1. **Create a new class** implementing `DiscountRule`:

```java
@Component
@Order(30) // Set execution order
public class SeasonalDiscountRule implements DiscountRule {
    
    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        // Define when this rule applies
        return isHolidaySeason(); // Example condition
    }
    
    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        if (isApplicable(orderSubtotal, userRole)) {
            return orderSubtotal.multiply(new BigDecimal("0.05"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }
}
```

2. **Spring will automatically inject it** into `DiscountService`
3. **No changes needed** to `DiscountService` or other rules

## Testing

### Unit Tests

Each discount rule has comprehensive unit tests:
- `PremiumUserDiscountRuleTest`: Tests premium user discount logic
- `LargeOrderExtraDiscountRuleTest`: Tests large order discount logic
- `DiscountServiceTest`: Tests discount service orchestration

### Test Coverage

- ✅ Rule applicability for different user roles
- ✅ Discount calculation accuracy
- ✅ Edge cases (zero amounts, thresholds)
- ✅ Multiple rules combining correctly
- ✅ Guardrails (discount capping)
- ✅ Rounding to 2 decimal places

## Suggested Enhancements

### 1. Configuration-Based Rules ⭐ **High Priority**

**Enhancement**: Make discount rules configurable via application properties

```yaml
discount:
  rules:
    premium-user:
      enabled: true
      percentage: 10
    large-order:
      enabled: true
      threshold: 500.00
      percentage: 5
```

**Benefits**:
- Change discount rates without code changes
- Enable/disable rules dynamically
- A/B testing capabilities
- Environment-specific configurations

**Implementation**:
- Create `@ConfigurationProperties` classes for each rule
- Inject configuration into rule implementations
- Use Spring profiles for different environments

### 2. Maximum Discount Cap ⭐ **High Priority**

**Enhancement**: Add configurable maximum discount percentage

```java
private static final BigDecimal MAX_DISCOUNT_PERCENTAGE = new BigDecimal("0.25"); // 25% max
```

**Benefits**:
- Prevent excessive discounts
- Business rule enforcement
- Financial control
- Protect profit margins

**Implementation**:
- Add to `DiscountService` guardrails
- Make configurable via properties
- Log when cap is applied

### 3. Discount History/Audit ⭐ **Medium Priority**

**Enhancement**: Log which rules applied and their contributions

```java
public class DiscountResult {
    private BigDecimal totalDiscount;
    private List<DiscountContribution> contributions;
    
    public static class DiscountContribution {
        private String ruleName;
        private BigDecimal amount;
        private boolean applied;
    }
}
```

**Benefits**:
- Audit trail for discounts
- Debugging discount calculations
- Analytics on discount effectiveness
- Compliance and reporting

**Implementation**:
- Return `DiscountResult` instead of just `BigDecimal`
- Store contributions in order entity
- Add audit logging

### 4. Time-Based Rules ⭐ **Medium Priority**

**Enhancement**: Add rules that apply based on time/date

```java
@Component
@Order(30)
public class HolidayDiscountRule implements DiscountRule {
    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        return isHolidaySeason() && orderSubtotal.compareTo(THRESHOLD) > 0;
    }
}
```

**Benefits**:
- Seasonal promotions
- Flash sales
- Time-limited discounts
- Marketing campaign support

**Implementation**:
- Add date/time utilities
- Create time-based rule base class
- Support for scheduled promotions

### 5. Product Category Discounts ⭐ **Medium Priority**

**Enhancement**: Discounts based on product categories

```java
@Component
@Order(40)
public class CategoryDiscountRule implements DiscountRule {
    // Apply discount if order contains specific product categories
    // Requires order items with product details
}
```

**Benefits**:
- Category-specific promotions
- Cross-selling incentives
- Inventory management tool
- Clearance sales

**Implementation**:
- Extend `DiscountRule` to accept order items
- Query product service for category information
- Cache category lookups

### 6. Customer Loyalty Discounts ⭐ **Low Priority**

**Enhancement**: Discounts based on customer order history

```java
@Component
@Order(50)
public class LoyaltyDiscountRule implements DiscountRule {
    // Apply discount based on total lifetime order value
    // Requires order history from order service
}
```

**Benefits**:
- Customer retention
- Reward loyal customers
- Increase customer lifetime value
- Competitive advantage

**Implementation**:
- Query order service for customer history
- Cache loyalty tiers
- Add loyalty tier to user entity

### 7. Conditional Rule Dependencies ⭐ **Low Priority**

**Enhancement**: Rules that depend on other rules

```java
@Component
@Order(60)
public class StackedDiscountRule implements DiscountRule {
    // Apply additional discount if multiple other rules are active
    // Requires access to other rule results
}
```

**Benefits**:
- Complex discount strategies
- Promotional bundles
- Marketing campaigns
- Advanced pricing models

**Implementation**:
- Pass rule context through pipeline
- Track which rules applied
- Support rule dependencies

### 8. Discount Validation ⭐ **Medium Priority**

**Enhancement**: Validate discount rules before applying

```java
public interface DiscountRule {
    // ... existing methods ...
    
    default void validate(BigDecimal orderSubtotal, UserRole userRole) {
        if (orderSubtotal == null || orderSubtotal.signum() < 0) {
            throw new IllegalArgumentException("Invalid order subtotal");
        }
    }
}
```

**Benefits**:
- Data integrity
- Early error detection
- Better error messages
- Defensive programming

**Implementation**:
- Add validation to interface
- Implement in base class or each rule
- Add validation tests

### 9. Performance Optimization ⭐ **Low Priority**

**Enhancement**: Cache rule applicability checks

```java
@Service
public class DiscountService {
    private final Cache<String, Boolean> applicabilityCache;
    
    // Cache isApplicable() results for common scenarios
}
```

**Benefits**:
- Reduced computation for repeated scenarios
- Better performance under load
- Scalability improvements
- Lower latency

**Implementation**:
- Use Spring Cache or Caffeine
- Cache key: orderSubtotal + userRole
- TTL-based expiration

### 10. Discount Analytics ⭐ **Medium Priority**

**Enhancement**: Track discount usage and effectiveness

```java
public class DiscountAnalytics {
    public void recordDiscountApplied(String ruleName, BigDecimal amount, UserRole role);
    public DiscountStatistics getStatistics();
}
```

**Benefits**:
- Business intelligence
- Rule effectiveness analysis
- ROI calculation for discounts
- Data-driven decisions

**Implementation**:
- Event-driven architecture
- Publish discount events to Kafka
- Aggregate statistics
- Dashboard for analytics

## Best Practices

1. **Always round to 2 decimal places** in discount calculations
2. **Return BigDecimal.ZERO.setScale(2)** when rule doesn't apply
3. **Use @Order annotation** to control rule execution order
4. **Keep rules independent** - don't create dependencies between rules
5. **Test edge cases** - zero amounts, very large amounts, threshold boundaries
6. **Document rule logic** - add JavaDoc explaining when rules apply
7. **Use meaningful rule names** - make it clear what each rule does
8. **Validate inputs** - check for null and negative values

## Code Examples

### Example: Adding a New Rule

```java
@Component
@Order(30)
public class FirstOrderDiscountRule implements DiscountRule {
    
    private final OrderRepository orderRepository;
    
    @Override
    public boolean isApplicable(BigDecimal orderSubtotal, UserRole userRole) {
        // Check if this is user's first order
        return orderRepository.countByUserId(userId) == 0;
    }
    
    @Override
    public BigDecimal calculateDiscount(BigDecimal orderSubtotal, UserRole userRole) {
        if (isApplicable(orderSubtotal, userRole)) {
            // 15% discount for first order
            return orderSubtotal.multiply(new BigDecimal("0.15"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }
}
```

## Summary

The Chain of Responsibility pattern provides:
- ✅ **Extensibility**: Easy to add new rules
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Testability**: Each rule can be tested independently
- ✅ **Flexibility**: Rules can be reordered, enabled, or disabled
- ✅ **Composability**: Multiple discounts apply additively
- ✅ **Scalability**: Can handle complex discount strategies

