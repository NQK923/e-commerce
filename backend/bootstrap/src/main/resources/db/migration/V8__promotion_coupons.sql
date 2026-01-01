-- Promotion coupons

CREATE TABLE coupons (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    seller_id VARCHAR(255),
    discount_type VARCHAR(50) NOT NULL,
    discount_value NUMERIC(19, 2) NOT NULL,
    min_order_amount NUMERIC(19, 2),
    min_order_currency VARCHAR(10),
    max_discount_amount NUMERIC(19, 2),
    max_discount_currency VARCHAR(10),
    usage_limit INT NOT NULL DEFAULT 0,
    used_count INT NOT NULL DEFAULT 0,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE coupon_products (
    coupon_id VARCHAR(255) NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (coupon_id, product_id)
);
