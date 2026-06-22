-- Dev Seed Data (extracted from V1__baseline.sql)
-- Run this script manually on local/dev database to seed smoke data.

INSERT INTO users (id, email, password, display_name, avatar_url, auth_provider, provider_user_id, shop_description, shop_banner_url, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'buyer@example.local', '$2a$10$z2DwVPpPiW4uU62OwtMaIe5HtnUv7u1QR7qGNbzhxO2ebTK8VF17u', 'Smoke Buyer', NULL, 'LOCAL', NULL, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000102', 'seller@example.local', '$2a$10$vUPOVE2tAvsq41xepEHBK.nwirVl89ztbrxUkF3B3gThtk/giJLXm', 'Smoke Seller', NULL, 'LOCAL', NULL, 'Dev smoke seller for local marketplace testing.', NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000103', 'admin@example.local', '$2a$10$z2DwVPpPiW4uU62OwtMaIe5HtnUv7u1QR7qGNbzhxO2ebTK8VF17u', 'Smoke Admin', NULL, 'LOCAL', NULL, NULL, NULL, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'CUSTOMER'),
    ('00000000-0000-0000-0000-000000000102', 'CUSTOMER'),
    ('00000000-0000-0000-0000-000000000102', 'SELLER'),
    ('00000000-0000-0000-0000-000000000103', 'ADMIN')
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
ON CONFLICT (id) DO NOTHING;

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
ON CONFLICT (id) DO NOTHING;

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
ON CONFLICT (code) DO NOTHING;

INSERT INTO inventory (id, warehouse_id)
VALUES 
    ('inv-smoke-main', 'warehouse-smoke'),
    ('DEFAULT_INVENTORY', 'warehouse-smoke')
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, currency, category_id, quantity, sold_count, seller_id, created_at, updated_at)
VALUES (
    'prod-smoke-1',
    'Smoke Product',
    'A product for smoke testing.',
    150000.00,
    'VND',
    'electronics',
    100,
    0,
    '00000000-0000-0000-0000-000000000102',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

INSERT INTO product_variants (sku, name, price, currency, quantity, product_id)
VALUES (
    'SMOKE-SKU-1',
    'Default Variant',
    150000.00,
    'VND',
    100,
    'prod-smoke-1'
) ON CONFLICT DO NOTHING;

INSERT INTO flash_sales (id, product_id, price, currency, original_price, original_currency, start_time, end_time, total_quantity, remaining_quantity, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000f51',
    'prod-smoke-1',
    100000.00,
    'VND',
    150000.00,
    'VND',
    NOW() - INTERVAL '1 day',
    NOW() + INTERVAL '1 day',
    50,
    50,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

