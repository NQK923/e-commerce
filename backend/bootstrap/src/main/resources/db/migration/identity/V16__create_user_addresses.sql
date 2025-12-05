CREATE TABLE user_addresses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    label VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    full_name VARCHAR(255),
    phone_number VARCHAR(50),
    line1 VARCHAR(255),
    line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);
