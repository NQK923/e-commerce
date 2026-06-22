-- Consolidated baseline schema (combining V1 to V15)
-- Excludes smoke test products that were seeded and then deleted.

-- 1. users
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    display_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    auth_provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    shop_description TEXT,
    shop_banner_url VARCHAR(1024),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_users_provider ON users(auth_provider, provider_user_id);

-- 2. user_roles
CREATE TABLE user_roles (
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- 3. seller_applications
CREATE TABLE seller_applications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    phone VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    status VARCHAR(50) NOT NULL,
    accepted_terms BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url VARCHAR(1024),
    cover_url VARCHAR(1024),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_seller_applications_user ON seller_applications(user_id);
CREATE INDEX idx_seller_applications_status ON seller_applications(status);

-- 4. user_addresses
CREATE TABLE user_addresses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    label VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    full_name VARCHAR(255),
    phone_number VARCHAR(50),
    line1 VARCHAR(255),
    line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100)
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

-- 5. products
CREATE TABLE products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC,
    currency VARCHAR(10),
    category_id VARCHAR(255),
    quantity INTEGER,
    sold_count INTEGER DEFAULT 0,
    seller_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 6. product_variants
CREATE TABLE product_variants (
    sku VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC,
    currency VARCHAR(10),
    quantity INTEGER,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

-- 7. product_images
CREATE TABLE product_images (
    id VARCHAR(255) PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    sort_order INT,
    primary_image BOOLEAN,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

-- 8. product_tags
CREATE TABLE product_tags (
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

-- 9. product_reports
CREATE TABLE product_reports (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id VARCHAR(255),
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_product_reports_product_id ON product_reports(product_id);
CREATE INDEX idx_product_reports_status ON product_reports(status);
CREATE INDEX idx_product_reports_created_at ON product_reports(created_at);

-- 10. product_reviews
CREATE TABLE product_reviews (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    abuse_report_count INT NOT NULL DEFAULT 0,
    seller_response TEXT,
    seller_response_at TIMESTAMP,
    seller_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_product_reviews_product_id ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_user_id ON product_reviews(user_id);
CREATE INDEX idx_product_reviews_user_created_at ON product_reviews(user_id, created_at);

-- 11. product_review_reports
CREATE TABLE product_review_reports (
    id VARCHAR(255) PRIMARY KEY,
    review_id VARCHAR(255) NOT NULL REFERENCES product_reviews(id) ON DELETE CASCADE,
    reporter_user_id VARCHAR(255) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_product_review_reports_review_id ON product_review_reports(review_id);
CREATE INDEX idx_product_review_reports_reporter ON product_review_reports(reporter_user_id);

-- 12. inventory
CREATE TABLE inventory (
    id VARCHAR(255) PRIMARY KEY,
    warehouse_id VARCHAR(255)
);

-- 13. inventory_items
CREATE TABLE inventory_items (
    inventory_id VARCHAR(255) REFERENCES inventory(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    available INT NOT NULL,
    reserved INT NOT NULL,
    PRIMARY KEY (inventory_id, product_id)
);

-- 14. carts
CREATE TABLE carts (
    id VARCHAR(255) PRIMARY KEY
);

-- 15. cart_items
CREATE TABLE cart_items (
    cart_id VARCHAR(255) REFERENCES carts(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    variant_sku VARCHAR(255) NOT NULL DEFAULT '',
    quantity INT NOT NULL,
    price VARCHAR(50),
    currency VARCHAR(10),
    PRIMARY KEY (cart_id, product_id, variant_sku)
);

-- 16. orders
CREATE TABLE orders (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    status VARCHAR(50),
    currency VARCHAR(10),
    total_amount VARCHAR(50),
    tracking_number VARCHAR(255),
    tracking_carrier VARCHAR(100),
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    return_status VARCHAR(50) NOT NULL DEFAULT 'NONE',
    return_reason TEXT,
    return_note TEXT,
    return_requested_at TIMESTAMP,
    return_resolved_at TIMESTAMP,
    refund_amount VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 17. order_items
CREATE TABLE order_items (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255),
    flash_sale_id VARCHAR(255),
    quantity INT NOT NULL,
    price VARCHAR(50),
    order_id VARCHAR(255) REFERENCES orders(id) ON DELETE CASCADE,
    variant_sku VARCHAR(255),
    seller_id VARCHAR(255)
);

CREATE INDEX idx_order_items_variant_sku ON order_items(variant_sku);
CREATE INDEX idx_order_items_seller_id ON order_items(seller_id);

-- 18. order_outbox
CREATE TABLE order_outbox (
    id VARCHAR(255) PRIMARY KEY,
    aggregate_id VARCHAR(255),
    type VARCHAR(255),
    payload TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 19. promotions
CREATE TABLE promotions (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255)
);

-- 20. coupons
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

-- 21. coupon_products
CREATE TABLE coupon_products (
    coupon_id VARCHAR(255) NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (coupon_id, product_id)
);

-- 22. promotion_usages
CREATE TABLE promotion_usages (
    id VARCHAR(255) PRIMARY KEY,
    promotion_code VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    used_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_promotion_usages_code ON promotion_usages(promotion_code);
CREATE INDEX idx_promotion_usages_user ON promotion_usages(user_id);
CREATE INDEX idx_promotion_usages_used_at ON promotion_usages(used_at);

-- 23. shipping_rates
CREATE TABLE shipping_rates (
    id VARCHAR(255) PRIMARY KEY,
    method_id VARCHAR(255),
    destination VARCHAR(255),
    cost VARCHAR(50),
    currency VARCHAR(10)
);

-- 24. chat_conversation
CREATE TABLE chat_conversation (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 25. chat_conversation_participants
CREATE TABLE chat_conversation_participants (
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    participant_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (conversation_id, participant_id)
);

-- 26. chat_message
CREATE TABLE chat_message (
    id VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    sender_id VARCHAR(255) NOT NULL,
    receiver_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE INDEX idx_chat_message_conversation_sent_at ON chat_message(conversation_id, sent_at);

-- 27. flash_sales
CREATE TABLE flash_sales (
    id UUID PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    original_price NUMERIC(19, 2) NOT NULL,
    original_currency VARCHAR(3) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    total_quantity INTEGER NOT NULL,
    remaining_quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_flash_sales_product_id ON flash_sales(product_id);
CREATE INDEX idx_flash_sales_status ON flash_sales(status);
CREATE INDEX idx_flash_sales_time ON flash_sales(start_time, end_time);

-- 28. payment_transactions
CREATE TABLE payment_transactions (
    reference VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_no VARCHAR(100),
    raw_payload TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payment_transactions_order ON payment_transactions(order_id);

-- 29. notifications
CREATE TABLE notifications (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);

-- 30. daily_sales_reports
CREATE TABLE daily_sales_reports (
    report_date DATE PRIMARY KEY,
    total_revenue NUMERIC(19, 2) NOT NULL,
    total_orders INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 31. otp_tokens
CREATE TABLE otp_tokens (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

-- 32. refresh_tokens
CREATE TABLE refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);


-- Seed Data has been extracted to dev_seeds.sql

