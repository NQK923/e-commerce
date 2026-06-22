-- V2__phase2_schema_updates.sql

-- 1. Cast price fields from VARCHAR to NUMERIC(19,2) safely
ALTER TABLE cart_items
    ALTER COLUMN price TYPE NUMERIC(19, 2) USING NULLIF(price, '')::numeric(19,2);

ALTER TABLE order_items
    ALTER COLUMN price TYPE NUMERIC(19, 2) USING NULLIF(price, '')::numeric(19,2);

ALTER TABLE orders
    ALTER COLUMN total_amount TYPE NUMERIC(19, 2) USING NULLIF(total_amount, '')::numeric(19,2),
    ALTER COLUMN refund_amount TYPE NUMERIC(19, 2) USING NULLIF(refund_amount, '')::numeric(19,2);

-- 2. Add retry and observability fields to order_outbox
ALTER TABLE order_outbox
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_error TEXT,
    ADD COLUMN next_retry_at TIMESTAMP,
    ADD COLUMN dead_letter_at TIMESTAMP;
