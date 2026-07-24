-- V1__init.sql
-- Restaurant Booking Service schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE restaurant_properties (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    city        VARCHAR(120) NOT NULL,
    address     VARCHAR(500),
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurant_properties_city ON restaurant_properties (city);

CREATE TABLE tables (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id  UUID NOT NULL REFERENCES restaurant_properties (id) ON DELETE CASCADE,
    table_number VARCHAR(20) NOT NULL,
    capacity     INTEGER NOT NULL CHECK (capacity > 0),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (property_id, table_number)
);

CREATE INDEX idx_tables_property ON tables (property_id);

CREATE TABLE restaurant_bookings (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_id         UUID NOT NULL REFERENCES tables (id),
    customer_id      UUID NOT NULL,
    reservation_date DATE NOT NULL,
    time_slot        VARCHAR(10) NOT NULL,           -- e.g. "19:30"
    party_size       INTEGER NOT NULL CHECK (party_size > 0),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurant_bookings_table ON restaurant_bookings (table_id);
CREATE INDEX idx_restaurant_bookings_customer ON restaurant_bookings (customer_id);

-- The core double-booking guard: a given table can hold at most one ACTIVE
-- (PENDING/CONFIRMED) booking for the same date + time slot. Enforced by
-- Postgres itself at commit time, so it holds even under concurrent requests -
-- the application-layer availability check in the service layer is just a
-- fast-path / better error message. Cancelled/completed rows are excluded so a
-- table freed up by a cancellation can be booked again.
CREATE UNIQUE INDEX uq_active_table_slot
    ON restaurant_bookings (table_id, reservation_date, time_slot)
    WHERE status IN ('PENDING', 'CONFIRMED');
