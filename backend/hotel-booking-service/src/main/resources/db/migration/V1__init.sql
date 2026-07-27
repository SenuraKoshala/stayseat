-- V1__init.sql
-- Hotel Booking Service schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gist; -- needed for the EXCLUDE constraint below

CREATE TABLE properties (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    city        VARCHAR(120) NOT NULL,
    address     VARCHAR(500),
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_properties_city ON properties (city);

CREATE TABLE rooms (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id      UUID NOT NULL REFERENCES properties (id) ON DELETE CASCADE,
    room_number      VARCHAR(20) NOT NULL,
    type             VARCHAR(20) NOT NULL CHECK (type IN ('SINGLE', 'DOUBLE', 'SUITE', 'DELUXE')),
    capacity         INTEGER NOT NULL CHECK (capacity > 0),
    price_per_night  NUMERIC(12, 2) NOT NULL CHECK (price_per_night >= 0),
    currency         VARCHAR(3) NOT NULL DEFAULT 'LKR',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (property_id, room_number)
);

CREATE INDEX idx_rooms_property ON rooms (property_id);

CREATE TABLE hotel_bookings (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id          UUID NOT NULL REFERENCES rooms (id),
    customer_id      UUID NOT NULL,
    check_in_date    DATE NOT NULL,
    check_out_date   DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    total_amount     NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    currency         VARCHAR(3) NOT NULL DEFAULT 'LKR',
    stay_range       DATERANGE GENERATED ALWAYS AS (daterange(check_in_date, check_out_date, '[)')) STORED,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

    CHECK (check_out_date > check_in_date),

    -- The core double-booking guard: for a given room, no two bookings in
    -- PENDING or CONFIRMED status may have overlapping stay_range values.
    -- This is enforced by Postgres itself at commit time, so it holds even
    -- under concurrent requests - the application-layer availability check
    -- in the service layer is just a fast-path / better error message.
    EXCLUDE USING gist (
        room_id WITH =,
        stay_range WITH &&
    ) WHERE (status IN ('PENDING', 'CONFIRMED'))
);

CREATE INDEX idx_hotel_bookings_room ON hotel_bookings (room_id);
CREATE INDEX idx_hotel_bookings_customer ON hotel_bookings (customer_id);
