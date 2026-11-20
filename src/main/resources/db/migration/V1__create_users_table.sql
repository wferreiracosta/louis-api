CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    surname           VARCHAR(255) NOT NULL,
    document          VARCHAR(255) NOT NULL UNIQUE,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,
    password_encoding VARCHAR(16),
    type              VARCHAR(255) NOT NULL
);
