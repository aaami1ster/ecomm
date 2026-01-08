-- Add soft delete support to orders table
-- Migration: V2__add_deleted_at_to_orders.sql

ALTER TABLE orders ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Index for better query performance when filtering deleted orders
CREATE INDEX IF NOT EXISTS idx_orders_deleted_at ON orders(deleted_at) WHERE deleted_at IS NULL;

