-- V2__seed.sql
-- Demo seed data for the prototype. Idempotent: fixed UUIDs + ON CONFLICT so it
-- is safe to run more than once (Flyway on a fresh DB, or manually on a running one).

INSERT INTO properties (id, name, city, address, description) VALUES
  ('11111111-1111-1111-1111-111111110001', 'Ocean Breeze Hotel',  'Colombo', '12 Marine Drive, Colombo 03', 'Beachfront rooms with sea views.'),
  ('11111111-1111-1111-1111-111111110002', 'Hill Country Resort', 'Kandy',   '5 Temple Road, Kandy',       'Cool-climate resort near the hills.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rooms (id, property_id, room_number, type, capacity, price_per_night, currency) VALUES
  ('11111111-2222-1111-1111-111111110001', '11111111-1111-1111-1111-111111110001', '101', 'DOUBLE', 2, 15000.00, 'LKR'),
  ('11111111-2222-1111-1111-111111110002', '11111111-1111-1111-1111-111111110001', '102', 'SUITE',  4, 30000.00, 'LKR'),
  ('11111111-2222-1111-1111-111111110003', '11111111-1111-1111-1111-111111110001', '201', 'SINGLE', 1,  9000.00, 'LKR'),
  ('11111111-2222-1111-1111-111111110004', '11111111-1111-1111-1111-111111110002', 'A1',  'DELUXE', 2, 22000.00, 'LKR'),
  ('11111111-2222-1111-1111-111111110005', '11111111-1111-1111-1111-111111110002', 'A2',  'DOUBLE', 3, 18000.00, 'LKR')
ON CONFLICT (id) DO NOTHING;
