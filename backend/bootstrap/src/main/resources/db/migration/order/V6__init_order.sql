CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    status VARCHAR(50),
    currency VARCHAR(10),
    total_amount VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id VARCHAR(255) PRIMARY KEY,
    product_id VARCHAR(255),
    quantity INT NOT NULL,
    price VARCHAR(50),
    order_id VARCHAR(255) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_outbox (
    id VARCHAR(255) PRIMARY KEY,
    aggregate_id VARCHAR(255),
    type VARCHAR(255),
    payload TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
