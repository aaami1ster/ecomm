-- User Service Database Migration
-- Migration: V3__insert_default_users.sql
-- Description: Insert default users for each role (USER, PREMIUM_USER, ADMIN)

-- Default USER role user
-- Email: user@example.com
-- Password: password123 (encoded as: encoded_password123)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'user@example.com',
    'encoded_password123',
    'John',
    'Doe',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default PREMIUM_USER role user
-- Email: premium@example.com
-- Password: password123 (encoded as: encoded_password123)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'premium@example.com',
    'encoded_password123',
    'Jane',
    'Smith',
    'PREMIUM_USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Default ADMIN role user
-- Email: admin@example.com
-- Password: password123 (encoded as: encoded_password123)
INSERT INTO users (email, password, first_name, last_name, role, created_at, updated_at)
VALUES (
    'admin@example.com',
    'encoded_password123',
    'Admin',
    'User',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

