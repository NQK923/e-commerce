CREATE TABLE IF NOT EXISTS daily_sales_reports (
    report_date DATE PRIMARY KEY,
    total_revenue NUMERIC(19, 2) NOT NULL,
    total_orders INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
