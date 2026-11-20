CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    amount          NUMERIC(12, 2) NOT NULL,
    transferring_id BIGINT REFERENCES wallets(id),
    receiving_id    BIGINT REFERENCES wallets(id),
    timestamp       TIMESTAMP NOT NULL
);
