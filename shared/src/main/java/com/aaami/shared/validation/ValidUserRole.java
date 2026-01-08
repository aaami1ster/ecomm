package com.aaami.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a UserRole enum value is one of the allowed values.
 * This annotation ensures that only valid UserRole enum values are accepted.
 */
@Documented
@Constraint(validatedBy = UserRoleValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserRole {
    String message() default "Invalid role. Valid values are: USER, PREMIUM_USER, ADMIN";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

