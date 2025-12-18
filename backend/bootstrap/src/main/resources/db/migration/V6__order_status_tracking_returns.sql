-- Normalize order statuses to new lifecycle
UPDATE orders SET status = 'PENDING' WHERE status IN ('CREATED', 'CONFIRMED');
UPDATE orders SET status = 'DELIVERED' WHERE status = 'COMPLETED';

ALTER TABLE orders
    ADD COLUMN tracking_number VARCHAR(255),
    ADD COLUMN tracking_carrier VARCHAR(100),
    ADD COLUMN shipped_at TIMESTAMP,
    ADD COLUMN delivered_at TIMESTAMP,
    ADD COLUMN return_status VARCHAR(50),
    ADD COLUMN return_reason TEXT,
    ADD COLUMN return_note TEXT,
    ADD COLUMN return_requested_at TIMESTAMP,
    ADD COLUMN return_resolved_at TIMESTAMP,
    ADD COLUMN refund_amount VARCHAR(50);

UPDATE orders SET return_status = 'NONE' WHERE return_status IS NULL;
