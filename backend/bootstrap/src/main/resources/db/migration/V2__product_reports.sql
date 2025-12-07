-- product
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
