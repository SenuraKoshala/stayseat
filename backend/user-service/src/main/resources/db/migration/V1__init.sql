-- V1__init.sql
-- User Service schema. One profile row per AuthUser, keyed by the same UUID.
-- Rows are created by consuming the UserRegistered event (no create endpoint).

CREATE TABLE user_profiles (
    user_id     UUID PRIMARY KEY,               -- = AuthUser.id
    first_name  VARCHAR(120),
    last_name   VARCHAR(120),
    phone       VARCHAR(40),
    image_url   VARCHAR(1000),                  -- nullable; local /uploads URL now, S3 in the cloud
    role        VARCHAR(30) NOT NULL,           -- denormalized copy from Auth
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
