-- User Service Database Migration
-- Migration: V3__insert_default_users.sql
-- Description: Insert default users for each role (USER, PREMIUM_USER, ADMIN)
-- Note: Passwords are BCrypt-encoded. All default users have password: password123

-- Default USER role user
-- Email: user@example.com
-- Password: password123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'user@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'John',
    'Doe',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default PREMIUM_USER role user
-- Email: premium@example.com
-- Password: password123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'premium@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Jane',
    'Smith',
    'PREMIUM_USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default ADMIN role user
-- Email: admin@example.com
-- Password: password123 (BCrypt encoded)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'User',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

