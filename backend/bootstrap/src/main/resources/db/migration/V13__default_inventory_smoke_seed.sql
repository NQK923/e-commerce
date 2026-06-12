-- Seed the inventory id used by order reservation and Redis warm-up.

INSERT INTO inventory (id, warehouse_id)
VALUES ('DEFAULT_INVENTORY', 'warehouse-smoke')
ON CONFLICT (id) DO UPDATE
SET warehouse_id = EXCLUDED.warehouse_id;

INSERT INTO inventory_items (inventory_id, product_id, available, reserved)
VALUES
    ('DEFAULT_INVENTORY', 'prod-smoke-laptop', 25, 0),
    ('DEFAULT_INVENTORY', 'prod-smoke-mouse', 80, 0),
    ('DEFAULT_INVENTORY', 'SMOKE-LAPTOP-14-16GB', 15, 0),
    ('DEFAULT_INVENTORY', 'SMOKE-LAPTOP-14-32GB', 10, 0)
ON CONFLICT (inventory_id, product_id) DO UPDATE
SET
    available = EXCLUDED.available,
    reserved = EXCLUDED.reserved;
