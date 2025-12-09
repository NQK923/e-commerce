ALTER TABLE products ALTER COLUMN price TYPE NUMERIC USING price::numeric;
ALTER TABLE product_variants ALTER COLUMN price TYPE NUMERIC USING price::numeric;
