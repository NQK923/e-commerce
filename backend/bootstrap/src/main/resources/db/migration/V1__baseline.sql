-- Consolidated baseline (includes previous V1-V12), keeps seed data

-- identity
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    display_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    auth_provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_users_provider ON users(auth_provider, provider_user_id);

CREATE TABLE user_roles (
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

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

-- seed admin
INSERT INTO users (id, email, password, auth_provider, provider_user_id, created_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555555', 'nqk1337@gmail.com', 'QWRtaW5AMTIz', 'LOCAL', NULL, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'nqk1337@gmail.com'
ON CONFLICT DO NOTHING;

-- product
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

CREATE TABLE product_variants (
    sku VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC,
    currency VARCHAR(10),
    quantity INTEGER,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_images (
    id VARCHAR(255) PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    sort_order INT,
    primary_image BOOLEAN,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_tags (
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

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

CREATE TABLE product_reviews (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_product_reviews_product_id ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_user_id ON product_reviews(user_id);

-- inventory
CREATE TABLE inventory (
    id VARCHAR(255) PRIMARY KEY,
    warehouse_id VARCHAR(255)
);

CREATE TABLE inventory_items (
    inventory_id VARCHAR(255) REFERENCES inventory(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    available INT NOT NULL,
    reserved INT NOT NULL,
    PRIMARY KEY (inventory_id, product_id)
);

-- cart
CREATE TABLE carts (
    id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE cart_items (
    cart_id VARCHAR(255) REFERENCES carts(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    variant_sku VARCHAR(255) NOT NULL DEFAULT '',
    quantity INT NOT NULL,
    price VARCHAR(50),
    currency VARCHAR(10),
    PRIMARY KEY (cart_id, product_id, variant_sku)
);

-- order
CREATE TABLE orders (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    status VARCHAR(50),
    currency VARCHAR(10),
    total_amount VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE order_items (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255),
    flash_sale_id VARCHAR(255),
    quantity INT NOT NULL,
    price VARCHAR(50),
    order_id VARCHAR(255) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_outbox (
    id VARCHAR(255) PRIMARY KEY,
    aggregate_id VARCHAR(255),
    type VARCHAR(255),
    payload TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- promotion
CREATE TABLE promotions (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255)
);

-- logistics
CREATE TABLE shipping_rates (
    id VARCHAR(255) PRIMARY KEY,
    method_id VARCHAR(255),
    destination VARCHAR(255),
    cost VARCHAR(50),
    currency VARCHAR(10)
);

-- chat
CREATE TABLE chat_conversation (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chat_conversation_participants (
    conversation_id VARCHAR(255) NOT NULL REFERENCES chat_conversation(id) ON DELETE CASCADE,
    participant_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (conversation_id, participant_id)
);

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

-- flash sale
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
