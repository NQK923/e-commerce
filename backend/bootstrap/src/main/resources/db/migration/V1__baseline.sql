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
    price VARCHAR(50),
    currency VARCHAR(10),
    category_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE product_variants (
    sku VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    price VARCHAR(50),
    currency VARCHAR(10),
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
    quantity INT NOT NULL,
    price VARCHAR(50),
    currency VARCHAR(10),
    PRIMARY KEY (cart_id, product_id)
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
