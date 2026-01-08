package com.aaami.shared.validation;

import com.aaami.shared.dto.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidUserRole annotation.
 * Validates that the provided UserRole is one of the allowed enum values.
 */
public class UserRoleValidator implements ConstraintValidator<ValidUserRole, UserRole> {

    @Override
    public void initialize(ValidUserRole constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(UserRole role, ConstraintValidatorContext context) {
        // Null values are considered valid (handled by @NotNull if required)
        if (role == null) {
            return true;
        }
        
        // Check if the role is one of the valid enum values
        try {
            UserRole.valueOf(role.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

