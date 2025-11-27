CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price VARCHAR(50),
    currency VARCHAR(10),
    category_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_variants (
    sku VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    price VARCHAR(50),
    currency VARCHAR(10),
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_images (
    id VARCHAR(255) PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    sort_order INT,
    primary_image BOOLEAN,
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_tags (
    product_id VARCHAR(255) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, tag)
);
