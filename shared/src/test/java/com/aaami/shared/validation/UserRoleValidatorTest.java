package com.aaami.shared.validation;

import com.aaami.shared.dto.UserRole;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleValidatorTest {

    private UserRoleValidator validator;
    
    @Mock
    private ValidUserRole constraintAnnotation;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new UserRoleValidator();
        validator.initialize(constraintAnnotation);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenRoleIsValid() {
        // Given
        UserRole role = UserRole.USER;

        // When
        boolean result = validator.isValid(role, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenRoleIsNull() {
        // Given
        UserRole role = null;

        // When
        boolean result = validator.isValid(role, context);

        // Then
        assertTrue(result); // Null is handled by @NotNull if required
    }

    @Test
    void isValid_ShouldReturnTrue_ForAllValidRoles() {
        // Given
        for (UserRole role : UserRole.values()) {
            // When
            boolean result = validator.isValid(role, context);

            // Then
            assertTrue(result, "Role " + role + " should be valid");
        }
    }
}

