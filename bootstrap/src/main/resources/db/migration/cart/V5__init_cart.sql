CREATE TABLE IF NOT EXISTS carts (
    id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_id VARCHAR(255) REFERENCES carts(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price VARCHAR(50),
    currency VARCHAR(10),
    PRIMARY KEY (cart_id, product_id)
);
