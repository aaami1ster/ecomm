-- Add soft delete support to users table
-- Migration: V2__add_deleted_at_to_users.sql

ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Index for better query performance when filtering deleted users
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;

