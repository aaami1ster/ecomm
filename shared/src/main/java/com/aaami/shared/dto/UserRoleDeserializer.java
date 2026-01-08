package com.aaami.shared.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for UserRole enum.
 * Provides better error messages when invalid role values are provided in JSON.
 */
public class UserRoleDeserializer extends JsonDeserializer<UserRole> {

    @Override
    public UserRole deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try to match case-insensitively and handle underscores
            String normalizedValue = value.trim().toUpperCase().replace("-", "_");
            return UserRole.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            // Provide a helpful error message with valid values
            String validValues = String.join(", ", 
                UserRole.USER.name(), 
                UserRole.PREMIUM_USER.name(), 
                UserRole.ADMIN.name());
            throw new IOException(
                String.format("Invalid role value '%s'. Valid values are: %s", value, validValues), e);
        }
    }
}

