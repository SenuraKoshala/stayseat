# Hotel Booking Service — StaySeat

Implements the Hotel Booking Service from `API_CONTRACT.md` §4.3:
`properties`, `rooms`, `hotel_bookings`, availability search, and
double-booking prevention.

## 1. Prerequisites
- JDK 21
- Maven (IntelliJ's bundled Maven is fine)
- PostgreSQL running locally

## 2. Database setup
```sql
CREATE DATABASE hotel_booking_db;
CREATE USER hotel_svc WITH PASSWORD 'hotel_pass';
GRANT ALL PRIVILEGES ON DATABASE hotel_booking_db TO hotel_svc;
```
Update `src/main/resources/application.yml` if you used different
credentials/port. No manual table creation needed — Flyway runs
`V1__init.sql` automatically on startup.

## 3. Open in IntelliJ
`File → Open` → select this folder → let it import as a Maven project.
Run `HotelBookingServiceApplication.main()`, or `mvn spring-boot:run`.

Service starts on **http://localhost:8083**. Swagger UI at
`http://localhost:8083/swagger-ui.html`.

## 4. Auth while Auth Service isn't ready yet
This service checks, in order:
1. `X-User-Id` / `X-User-Role` headers (what the API Gateway will send once it exists)
2. A real `Authorization: Bearer <jwt>` signed with the shared secret

For local testing **before** the Gateway/Auth service exist, the easiest
path is option 1 — just send the headers directly, e.g.:
```
X-User-Id: 11111111-1111-1111-1111-111111111111
X-User-Role: HOTEL_ADMIN
```
Once the Auth Service owner shares a real signing secret, put it in the
`JWT_SECRET` environment variable (don't hardcode it) and you can test with
real tokens too.

## 5. Try it (curl)

Create a property (as HOTEL_ADMIN):
```bash
curl -X POST http://localhost:8083/api/v1/hotel/properties \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 11111111-1111-1111-1111-111111111111" \
  -H "X-User-Role: HOTEL_ADMIN" \
  -d '{"name":"Ruhuna Grand","city":"Matara","address":"Main St"}'
```

Add a room (grab the `propertyId` from the response above):
```bash
curl -X POST http://localhost:8083/api/v1/hotel/rooms \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 11111111-1111-1111-1111-111111111111" \
  -H "X-User-Role: HOTEL_ADMIN" \
  -d '{"propertyId":"<paste-id>","roomNumber":"101","type":"DOUBLE","capacity":2,"pricePerNight":8500}'
```

Search availability (no auth needed):
```bash
curl "http://localhost:8083/api/v1/hotel/availability?propertyId=<paste-id>&checkIn=2026-08-10&checkOut=2026-08-12"
```

Book it (as CUSTOMER):
```bash
curl -X POST http://localhost:8083/api/v1/hotel/bookings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -H "X-User-Role: CUSTOMER" \
  -d '{"roomId":"<paste-room-id>","checkInDate":"2026-08-10","checkOutDate":"2026-08-12"}'
```

Try the same booking again — you should get a `409 ROOM_NOT_AVAILABLE`.
That's the Postgres exclusion constraint (see `V1__init.sql`) doing its job.

## 6. What's already handled vs. what's next
**Done:**
- Full CRUD/search per the contract's endpoint table
- Double-booking prevention at the DB level (`EXCLUDE USING gist`), not just app-layer
- Standard success/error envelopes (contract §3.4/§3.5)
- Owner-or-admin checks on booking read/cancel
- Event publishing (`HotelBookingCreated/Confirmed/Cancelled`) behind an
  `EventPublisher` interface — currently logs to console via
  `LoggingEventPublisher`

**Not done yet (needs the rest of the team / later phases):**
- No real message broker wired in — swap `LoggingEventPublisher` for a
  Kafka/RabbitMQ implementation once the team picks one (contract §5)
- `PATCH /bookings/{id}/confirm` only accepts `HOTEL_ADMIN` right now. The
  contract also allows an **internal call from Payment Service** on
  successful charge — that needs a service-to-service auth mechanism
  (e.g. an internal API key or trusting the Gateway's `X-Internal-Call`
  header) once Payment Service exists. Left as a TODO in `BookingController`.
- No integration tests yet (Testcontainers + the real exclusion constraint
  would be the right way to test the double-booking logic properly)
- JWT secret is a dev placeholder — get the real shared secret from
  whoever owns Auth Service before integrating for real
