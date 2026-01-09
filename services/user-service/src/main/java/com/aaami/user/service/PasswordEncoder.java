package com.aaami.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Password encoder service using BCryptPasswordEncoder from Spring Security.
 * 
 * BCrypt is a strong, adaptive hashing function that:
 * - Automatically handles salt generation
 * - Is resistant to rainbow table attacks
 * - Has configurable strength (cost factor)
 * - Is widely used in production systems
 */
@Service
public class PasswordEncoder {
    
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    public PasswordEncoder() {
        // Use BCrypt with strength 10 (default, good balance of security and performance)
        // Strength 10 means 2^10 = 1024 rounds
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }
    
    /**
     * Encodes a raw password using BCrypt.
     * 
     * @param rawPassword The raw password to encode
     * @return BCrypt-encoded password (starts with $2a$)
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Verifies if a raw password matches an encoded password.
     * 
     * @param rawPassword The raw password to verify
     * @param encodedPassword The encoded password to check against
     * @return true if the passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

