package com.aaami.gateway.config;

/**
 * API version constants for versioning endpoints.
 * All API endpoints should use these constants to ensure consistency.
 */
public final class ApiVersion {
    
    /**
     * Current API version prefix
     */
    public static final String V1 = "/api/v1";
    
    /**
     * Base API path (for backward compatibility)
     */
    public static final String BASE = "/api";
    
    private ApiVersion() {
        // Utility class - prevent instantiation
    }
}

