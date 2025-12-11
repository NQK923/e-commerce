-- Ensure products table stores seller identifier expected by ProductEntity
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS seller_id VARCHAR(255);
