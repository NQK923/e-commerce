CREATE TABLE promotion_usages (
    id VARCHAR(255) PRIMARY KEY,
    promotion_code VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    used_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_promotion_usages_code ON promotion_usages(promotion_code);
CREATE INDEX idx_promotion_usages_user ON promotion_usages(user_id);
CREATE INDEX idx_promotion_usages_used_at ON promotion_usages(used_at);
