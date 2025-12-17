-- Add variant SKU support for order items
ALTER TABLE order_items
ADD COLUMN IF NOT EXISTS variant_sku VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_order_items_variant_sku ON order_items(variant_sku);
