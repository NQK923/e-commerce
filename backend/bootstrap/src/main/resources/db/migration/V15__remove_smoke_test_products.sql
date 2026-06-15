-- Clean up dev smoke-test products from all related tables
DELETE FROM flash_sales WHERE product_id = 'prod-smoke-mouse';
DELETE FROM coupon_products WHERE product_id IN ('prod-smoke-laptop', 'prod-smoke-mouse');
DELETE FROM product_tags WHERE product_id IN ('prod-smoke-laptop', 'prod-smoke-mouse');
DELETE FROM product_images WHERE product_id IN ('prod-smoke-laptop', 'prod-smoke-mouse');
DELETE FROM product_variants WHERE product_id = 'prod-smoke-laptop';
DELETE FROM inventory_items WHERE product_id IN ('prod-smoke-laptop', 'prod-smoke-mouse', 'SMOKE-LAPTOP-14-16GB', 'SMOKE-LAPTOP-14-32GB');
DELETE FROM products WHERE id IN ('prod-smoke-laptop', 'prod-smoke-mouse');
