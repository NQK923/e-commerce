CREATE TABLE IF NOT EXISTS inventory (
    id VARCHAR(255) PRIMARY KEY,
    warehouse_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inventory_items (
    inventory_id VARCHAR(255) REFERENCES inventory(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    available INT NOT NULL,
    reserved INT NOT NULL,
    PRIMARY KEY (inventory_id, product_id)
);
