# Restaurant Booking Service

StaySeat — Restaurant Table Reservation Service. Implements **Section 4.4** of
`StaySeat_Implementation_Guide_and_API_Contract.md`. Mirrors the Hotel Booking
Service's structure and conventions.

- **Port:** `8084`
- **Base path:** `/api/v1/restaurant`
- **Owns tables:** `restaurant_properties`, `tables`, `restaurant_bookings`
- **Schema:** owned by Flyway (`db/migration/V1__init.sql`), not Hibernate
  (`ddl-auto: validate`).

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | `/properties?city=` | none |
| POST | `/properties` | RESTAURANT_ADMIN |
| GET | `/properties/{propertyId}/tables` | none |
| POST | `/tables` | RESTAURANT_ADMIN |
| GET | `/availability?propertyId=&date=&timeSlot=&partySize=` | none |
| POST | `/bookings` | CUSTOMER |
| GET | `/bookings/{id}` | owner or admin |
| GET | `/bookings/me` | CUSTOMER |
| PATCH | `/bookings/{id}/confirm` | RESTAURANT_ADMIN |
| PATCH | `/bookings/{id}/cancel` | owner or admin |

> `POST /properties` and `POST /tables` aren't listed in the contract's endpoint
> table but are added for parity with the Hotel service so admins can seed data.

## Double-booking prevention

A partial unique index `uq_active_table_slot` on
`(table_id, reservation_date, time_slot) WHERE status IN ('PENDING','CONFIRMED')`
guarantees at most one active booking per table/date/slot at the DB level, even
under concurrent requests. The service-layer availability check is just a
fast-path; the DB index is the final guard. A violation surfaces as
`409 TABLE_NOT_AVAILABLE`.

## Events published

`RestaurantBookingCreated`, `RestaurantBookingConfirmed`, `RestaurantBookingCancelled`.
Currently routed through `LoggingEventPublisher` (logs only) — same placeholder
approach as the Hotel service. Swap in a RabbitMQ publisher (topic exchange
`stayseat.exchange`, routing key `restaurant.confirmed`) to feed the Notification
Service queue.

## Error codes

`NOT_FOUND`, `TABLE_NOT_AVAILABLE`, `VALIDATION_ERROR`, `FORBIDDEN`,
`UNAUTHENTICATED`, `INTERNAL_ERROR`.

## Local setup

Create the database and role, then run:

```sql
CREATE DATABASE restaurant_booking;
CREATE USER restaurant_svc WITH PASSWORD 'restaurant_pass';
GRANT ALL PRIVILEGES ON DATABASE restaurant_booking TO restaurant_svc;
```

```bash
# from backend/restaurant-booking-service
mvn spring-boot:run
```

Set `JWT_SECRET` to the same value the Auth Service uses so Bearer tokens verify.
