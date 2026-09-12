-- V1__init.sql
-- Payment Service schema (API_CONTRACT.md §4.5)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id          UUID NOT NULL,
    booking_type        VARCHAR(20) NOT NULL CHECK (booking_type IN ('HOTEL', 'RESTAURANT')),
    customer_id         UUID NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency            VARCHAR(3) NOT NULL DEFAULT 'LKR',
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    gateway_reference   VARCHAR(100),
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_booking ON transactions (booking_id);
CREATE INDEX idx_transactions_customer ON transactions (customer_id);

-- A given gateway reference must be unique so webhook callbacks can be looked
-- up unambiguously and can't collide across transactions.
CREATE UNIQUE INDEX uq_transactions_gateway_reference
    ON transactions (gateway_reference)
    WHERE gateway_reference IS NOT NULL;

-- Idempotency guard: only one SUCCEEDED transaction is allowed per booking.
-- This backs up the application-layer check in PaymentServiceImpl so a
-- retried /charge call can never double-charge a booking even under
-- concurrent requests.
CREATE UNIQUE INDEX uq_transactions_booking_succeeded
    ON transactions (booking_id)
    WHERE status = 'SUCCEEDED';
