-- V2__seed.sql
-- Demo seed data for the prototype. Idempotent: fixed UUIDs + ON CONFLICT so it
-- is safe to run more than once (Flyway on a fresh DB, or manually on a running one).

INSERT INTO restaurant_properties (id, name, city, address, description) VALUES
  ('22222222-1111-1111-1111-111111110001', 'The Spice Garden', 'Colombo', '80 Galle Road, Colombo 04', 'Authentic Sri Lankan cuisine.'),
  ('22222222-1111-1111-1111-111111110002', 'Lakeview Bistro',  'Kandy',   'Lake Round, Kandy',         'Lakeside dining with a modern menu.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tables (id, property_id, table_number, capacity) VALUES
  ('22222222-2222-1111-1111-111111110001', '22222222-1111-1111-1111-111111110001', 'T1', 2),
  ('22222222-2222-1111-1111-111111110002', '22222222-1111-1111-1111-111111110001', 'T2', 4),
  ('22222222-2222-1111-1111-111111110003', '22222222-1111-1111-1111-111111110001', 'T5', 6),
  ('22222222-2222-1111-1111-111111110004', '22222222-1111-1111-1111-111111110002', 'L1', 2),
  ('22222222-2222-1111-1111-111111110005', '22222222-1111-1111-1111-111111110002', 'L2', 4)
ON CONFLICT (id) DO NOTHING;
