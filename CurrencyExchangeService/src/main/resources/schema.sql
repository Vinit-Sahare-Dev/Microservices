CREATE TABLE currency_exchange (
    id BIGINT PRIMARY KEY,
    currency_from VARCHAR(3) NOT NULL,
    currency_to VARCHAR(3) NOT NULL,
    conversion_multiple DECIMAL(10,2) NOT NULL,
    environment VARCHAR(50)
);