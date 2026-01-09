-- User Service Database Migration
-- Migration: V3__insert_default_users.sql
-- Description: Insert default users for each role (USER, PREMIUM_USER, ADMIN)
-- Note: Passwords are BCrypt-encoded with cost factor 10. All default users have password: password123
-- 
-- To generate a new hash, use:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
-- String hash = encoder.encode("password123");

-- Default USER role user
-- Email: user@example.com
-- Password: password123 (BCrypt encoded with cost factor 10)
-- NOTE: This hash was verified to match "password123" using BCryptPasswordEncoder(10)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'user@example.com',
    '$2a$10$dN0IvjNvwOWWR72pr/8fPONDz3cmBG0.M17LKUfahbm4II7KL/gY6',
    'John',
    'Doe',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default PREMIUM_USER role user
-- Email: premium@example.com
-- Password: password123 (BCrypt encoded with cost factor 10)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'premium@example.com',
    '$2a$10$dN0IvjNvwOWWR72pr/8fPONDz3cmBG0.M17LKUfahbm4II7KL/gY6',
    'Jane',
    'Smith',
    'PREMIUM_USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default ADMIN role user
-- Email: admin@example.com
-- Password: password123 (BCrypt encoded with cost factor 10)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'admin@example.com',
    '$2a$10$dN0IvjNvwOWWR72pr/8fPONDz3cmBG0.M17LKUfahbm4II7KL/gY6',
    'Admin',
    'User',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

