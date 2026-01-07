package com.aaami.user.service;

import org.springframework.stereotype.Service;

/**
 * Simple password encoder service.
 * In production, use BCryptPasswordEncoder from Spring Security.
 */
@Service
public class PasswordEncoder {
    
    // TODO: Replace with proper BCryptPasswordEncoder in production
    public String encode(String rawPassword) {
        // This is a placeholder - in production, use BCryptPasswordEncoder
        return "encoded_" + rawPassword; // Simplified for demo
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        // This is a placeholder - in production, use BCryptPasswordEncoder
        return encodedPassword.equals("encoded_" + rawPassword);
    }
}

