ALTER TABLE product_reviews
    ADD COLUMN verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN abuse_report_count INT NOT NULL DEFAULT 0,
    ADD COLUMN seller_response TEXT,
    ADD COLUMN seller_response_at TIMESTAMP,
    ADD COLUMN seller_id VARCHAR(255);

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
CREATE INDEX idx_product_reviews_user_created_at ON product_reviews(user_id, created_at);
