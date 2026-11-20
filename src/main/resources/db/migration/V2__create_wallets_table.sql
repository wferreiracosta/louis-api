CREATE TABLE wallets (
    id           BIGSERIAL PRIMARY KEY,
    amount       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_date TIMESTAMP,
    update_date  TIMESTAMP,
    user_id      BIGINT UNIQUE REFERENCES users(id)
);
