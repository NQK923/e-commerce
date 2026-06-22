-- Add soft delete column to products table
ALTER TABLE products ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
