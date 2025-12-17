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
