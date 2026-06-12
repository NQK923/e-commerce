-- Repeatable local smoke-test data for core buyer/seller/catalog flows.
-- These accounts are intentionally dev-only and use the simple local password hasher.

INSERT INTO users (id, email, password, display_name, avatar_url, auth_provider, provider_user_id, shop_description, shop_banner_url, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'buyer@example.local', 'QnV5ZXJAMTIz', 'Smoke Buyer', NULL, 'LOCAL', NULL, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000102', 'seller@example.local', 'U2VsbGVyQDEyMw==', 'Smoke Seller', NULL, 'LOCAL', NULL, 'Dev smoke seller for local marketplace testing.', NULL, NOW(), NOW())
ON CONFLICT (email) DO UPDATE
SET
    display_name = EXCLUDED.display_name,
    shop_description = EXCLUDED.shop_description,
    updated_at = NOW();

INSERT INTO user_roles (user_id, role)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'CUSTOMER'),
    ('00000000-0000-0000-0000-000000000102', 'CUSTOMER'),
    ('00000000-0000-0000-0000-000000000102', 'SELLER')
ON CONFLICT DO NOTHING;

INSERT INTO user_addresses (id, user_id, label, is_default, full_name, phone_number, line1, line2, city, state, postal_code, country)
VALUES (
    '00000000-0000-0000-0000-00000000a101',
    '00000000-0000-0000-0000-000000000101',
    'Home',
    TRUE,
    'Smoke Buyer',
    '+84900000001',
    '1 Dev Street',
    'Smoke District',
    'Ho Chi Minh City',
    'Ho Chi Minh',
    '700000',
    'VN'
)
ON CONFLICT (id) DO UPDATE
SET
    is_default = EXCLUDED.is_default,
    full_name = EXCLUDED.full_name,
    phone_number = EXCLUDED.phone_number,
    line1 = EXCLUDED.line1,
    city = EXCLUDED.city;

INSERT INTO seller_applications (id, user_id, store_name, contact_email, phone, category, description, status, accepted_terms, avatar_url, cover_url, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-00000000b102',
    '00000000-0000-0000-0000-000000000102',
    'Smoke Seller Store',
    'seller@example.local',
    '+84900000002',
    'electronics',
    'Approved local smoke seller application.',
    'APPROVED',
    TRUE,
    NULL,
    NULL,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO UPDATE
SET
    store_name = EXCLUDED.store_name,
    status = EXCLUDED.status,
    accepted_terms = EXCLUDED.accepted_terms,
    updated_at = NOW();

INSERT INTO products (id, name, description, price, currency, category_id, quantity, sold_count, seller_id, created_at, updated_at)
VALUES
    ('prod-smoke-laptop', 'Smoke Laptop Pro 14', 'Reliable local seed laptop for buyer checkout and seller order smoke tests.', 18990000, 'VND', 'electronics', 25, 0, '00000000-0000-0000-0000-000000000102', NOW(), NOW()),
    ('prod-smoke-mouse', 'Smoke Wireless Mouse', 'Accessory product for cart, search, and promotion smoke tests.', 499000, 'VND', 'electronics', 80, 0, '00000000-0000-0000-0000-000000000102', NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    category_id = EXCLUDED.category_id,
    quantity = EXCLUDED.quantity,
    seller_id = EXCLUDED.seller_id,
    updated_at = NOW();

INSERT INTO product_variants (sku, name, price, currency, quantity, product_id)
VALUES
    ('SMOKE-LAPTOP-14-16GB', '14 inch / 16GB RAM', 18990000, 'VND', 15, 'prod-smoke-laptop'),
    ('SMOKE-LAPTOP-14-32GB', '14 inch / 32GB RAM', 22990000, 'VND', 10, 'prod-smoke-laptop')
ON CONFLICT (sku) DO UPDATE
SET
    name = EXCLUDED.name,
    price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    quantity = EXCLUDED.quantity,
    product_id = EXCLUDED.product_id;

INSERT INTO product_images (id, url, sort_order, primary_image, product_id)
VALUES
    ('img-smoke-laptop-1', 'https://placehold.co/900x700/png?text=Smoke+Laptop', 0, TRUE, 'prod-smoke-laptop'),
    ('img-smoke-mouse-1', 'https://placehold.co/900x700/png?text=Smoke+Mouse', 0, TRUE, 'prod-smoke-mouse')
ON CONFLICT (id) DO UPDATE
SET
    url = EXCLUDED.url,
    sort_order = EXCLUDED.sort_order,
    primary_image = EXCLUDED.primary_image,
    product_id = EXCLUDED.product_id;

INSERT INTO product_tags (product_id, tag)
VALUES
    ('prod-smoke-laptop', 'smoke'),
    ('prod-smoke-laptop', 'laptop'),
    ('prod-smoke-mouse', 'smoke'),
    ('prod-smoke-mouse', 'accessory')
ON CONFLICT DO NOTHING;

INSERT INTO inventory (id, warehouse_id)
VALUES ('inv-smoke-main', 'warehouse-smoke')
ON CONFLICT (id) DO UPDATE
SET warehouse_id = EXCLUDED.warehouse_id;

INSERT INTO inventory_items (inventory_id, product_id, available, reserved)
VALUES
    ('inv-smoke-main', 'prod-smoke-laptop', 25, 0),
    ('inv-smoke-main', 'prod-smoke-mouse', 80, 0),
    ('inv-smoke-main', 'SMOKE-LAPTOP-14-16GB', 15, 0),
    ('inv-smoke-main', 'SMOKE-LAPTOP-14-32GB', 10, 0)
ON CONFLICT (inventory_id, product_id) DO UPDATE
SET
    available = EXCLUDED.available,
    reserved = EXCLUDED.reserved;

INSERT INTO coupons (id, code, seller_id, discount_type, discount_value, min_order_amount, min_order_currency, max_discount_amount, max_discount_currency, usage_limit, used_count, start_at, end_at, created_at, updated_at)
VALUES (
    'coupon-smoke-welcome',
    'SMOKE10',
    '00000000-0000-0000-0000-000000000102',
    'PERCENTAGE',
    10.00,
    100000.00,
    'VND',
    500000.00,
    'VND',
    100,
    0,
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '365 days',
    NOW(),
    NOW()
)
ON CONFLICT (code) DO UPDATE
SET
    seller_id = EXCLUDED.seller_id,
    discount_type = EXCLUDED.discount_type,
    discount_value = EXCLUDED.discount_value,
    min_order_amount = EXCLUDED.min_order_amount,
    min_order_currency = EXCLUDED.min_order_currency,
    max_discount_amount = EXCLUDED.max_discount_amount,
    max_discount_currency = EXCLUDED.max_discount_currency,
    usage_limit = EXCLUDED.usage_limit,
    updated_at = NOW();

INSERT INTO coupon_products (coupon_id, product_id)
VALUES
    ('coupon-smoke-welcome', 'prod-smoke-laptop'),
    ('coupon-smoke-welcome', 'prod-smoke-mouse')
ON CONFLICT DO NOTHING;

INSERT INTO flash_sales (id, product_id, price, currency, original_price, original_currency, start_time, end_time, total_quantity, remaining_quantity, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-00000000f101',
    'prod-smoke-mouse',
    399000.00,
    'VND',
    499000.00,
    'VND',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '365 days',
    30,
    30,
    'ACTIVE',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO UPDATE
SET
    product_id = EXCLUDED.product_id,
    price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    original_price = EXCLUDED.original_price,
    original_currency = EXCLUDED.original_currency,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    total_quantity = EXCLUDED.total_quantity,
    remaining_quantity = EXCLUDED.remaining_quantity,
    status = EXCLUDED.status,
    updated_at = NOW();
