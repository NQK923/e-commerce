UPDATE cart_items SET variant_sku = '' WHERE variant_sku IS NULL;
ALTER TABLE cart_items ALTER COLUMN variant_sku SET DEFAULT '';
ALTER TABLE cart_items ALTER COLUMN variant_sku SET NOT NULL;
ALTER TABLE cart_items DROP CONSTRAINT cart_items_pkey;
ALTER TABLE cart_items ADD PRIMARY KEY (cart_id, product_id, variant_sku);
