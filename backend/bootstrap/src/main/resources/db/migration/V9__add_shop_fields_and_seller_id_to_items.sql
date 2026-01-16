-- Add shop details to users table
ALTER TABLE users ADD COLUMN shop_description TEXT;
ALTER TABLE users ADD COLUMN shop_banner_url VARCHAR(1024);

-- Add seller_id to order_items table
ALTER TABLE order_items ADD COLUMN seller_id VARCHAR(255);

-- Create index for filtering orders by seller
CREATE INDEX idx_order_items_seller_id ON order_items(seller_id);
