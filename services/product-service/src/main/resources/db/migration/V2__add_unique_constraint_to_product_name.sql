-- Product Service Database Migration
-- Migration: V2__add_unique_constraint_to_product_name.sql
-- Description: Add unique constraint to product name (only for non-deleted products)

-- Drop the existing index on name if it exists (we'll replace it with a unique constraint)
DROP INDEX IF EXISTS idx_products_name;

-- Create a unique partial index on product name for non-deleted products
-- This ensures product names are unique only among active (non-deleted) products
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_name_unique 
ON products(name) 
WHERE deleted_at IS NULL;

