package com.aaami.exception;

import com.aaami.shared.exception.BaseGlobalExceptionHandler;
import com.aaami.shared.exception.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Order service exception handler.
 * Extends BaseGlobalExceptionHandler for common exception handling.
 * Add order-specific exception handlers here if needed.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
    // Order-specific exception handlers can be added here
}

