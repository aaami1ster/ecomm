package com.aaami.user.util;

import com.aaami.user.service.PasswordEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify and generate BCrypt hashes for migration files.
 * This test generates a verified hash for "password123" that can be used in V3 migration.
 */
class BcryptHashVerifierTest {

    @Test
    void generateVerifiedHashForMigration() {
        PasswordEncoder encoder = new PasswordEncoder();
        String password = "password123";
        
        // Generate a new hash
        String hash = encoder.encode(password);
        
        // Verify it works
        boolean matches = encoder.matches(password, hash);
        
        // Print for manual copy to migration file
        System.err.println("\n" + "=".repeat(60));
        System.err.println("BCRYPT HASH FOR MIGRATION FILE");
        System.err.println("=".repeat(60));
        System.err.println("Password: " + password);
        System.err.println("Hash: " + hash);
        System.err.println("Verification: " + (matches ? "SUCCESS" : "FAILED"));
        System.err.println("=".repeat(60) + "\n");
        
        // Assert it works
        assertTrue(matches, "Generated hash must match password");
        assertTrue(hash.startsWith("$2a$10$"), "Hash must be BCrypt with cost factor 10");
        assertEquals(60, hash.length(), "BCrypt hash must be 60 characters");
    }
    
    @Test
    void verifyCurrentMigrationHash() {
        PasswordEncoder encoder = new PasswordEncoder();
        String password = "password123";
        // This is the hash currently in V3__insert_default_users.sql
        String migrationHash = "$2a$10$dN0IvjNvwOWWR72pr/8fPONDz3cmBG0.M17LKUfahbm4II7KL/gY6";
        
        boolean matches = encoder.matches(password, migrationHash);
        
        System.err.println("\n" + "=".repeat(60));
        System.err.println("VERIFYING MIGRATION HASH");
        System.err.println("=".repeat(60));
        System.err.println("Password: " + password);
        System.err.println("Migration Hash: " + migrationHash);
        System.err.println("Verification: " + (matches ? "SUCCESS ✓" : "FAILED ✗"));
        System.err.println("=".repeat(60) + "\n");
        
        // This will fail if hash doesn't match - that's intentional to alert us
        assertTrue(matches, "Migration hash must match 'password123'");
    }
}

