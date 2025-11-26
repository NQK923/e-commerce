CREATE TABLE IF NOT EXISTS shipping_rates (
    id VARCHAR(255) PRIMARY KEY,
    method_id VARCHAR(255),
    destination VARCHAR(255),
    cost VARCHAR(50),
    currency VARCHAR(10)
);
