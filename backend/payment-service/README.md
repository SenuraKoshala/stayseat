# Payment Service — StaySeat

Implements the Payment Service from `API_CONTRACT.md` §4.5: `transactions`,
synchronous charge processing against a mock gateway, and the
`PaymentProcessed` / `PaymentFailed` events that Hotel Booking, Restaurant
Booking, and Notification services subscribe to.

## 1. Prerequisites
- JDK 17
- Maven (IntelliJ's bundled Maven is fine)
- PostgreSQL running locally
- RabbitMQ running locally (see `backend/infrastructure/docker-compose.yml`,
  or `docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:4-management`)

## 2. Database setup
```sql
CREATE DATABASE payment_db;
CREATE USER payment_svc WITH PASSWORD 'payment_pass';
GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_svc;
```
Update `src/main/resources/application.yml` if you used different
credentials/port. No manual table creation needed — Flyway runs
`V1__init.sql` automatically on startup.

## 3. Open in IntelliJ
`File → Open` → select this folder → let it import as a Maven project.
Run `PaymentServiceApplication.main()`, or `mvn spring-boot:run`.

Service starts on **http://localhost:8085**. Swagger UI at
`http://localhost:8085/swagger-ui.html`.

## 4. Auth while Auth Service isn't ready yet
Same pattern as Hotel Booking Service — this service checks, in order:
1. `X-User-Id` / `X-User-Role` headers (what the API Gateway will send once it exists)
2. A real `Authorization: Bearer <jwt>` signed with the shared secret

For local testing before the Gateway/Auth service exist:
```
X-User-Id: 22222222-2222-2222-2222-222222222222
X-User-Role: CUSTOMER
```
Put the real signing secret in `JWT_SECRET` once Auth Service shares it.

The one exception is `POST /webhook` — it's public and authenticated by an
HMAC-SHA256 signature instead (see §6 below), matching a real gateway's
webhook model.

## 5. Try it (curl)

Charge a booking (as CUSTOMER):
```bash
curl -X POST http://localhost:8085/api/v1/payments/charge \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -H "X-User-Role: CUSTOMER" \
  -d '{
        "bookingId": "33333333-3333-3333-3333-333333333333",
        "bookingType": "HOTEL",
        "amount": { "amount": 4500.00, "currency": "LKR" }
      }'
```

Demo a declined payment (`simulateFailure` is a local-only convenience flag,
not part of the official contract):
```bash
curl -X POST http://localhost:8085/api/v1/payments/charge \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -H "X-User-Role: CUSTOMER" \
  -d '{
        "bookingId": "44444444-4444-4444-4444-444444444444",
        "bookingType": "HOTEL",
        "amount": { "amount": 4500.00, "currency": "LKR" },
        "simulateFailure": true
      }'
```

Look up a transaction:
```bash
curl http://localhost:8085/api/v1/payments/{id} \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -H "X-User-Role: CUSTOMER"
```

Look up all transactions for a booking:
```bash
curl http://localhost:8085/api/v1/payments/booking/{bookingId} \
  -H "X-User-Id: 22222222-2222-2222-2222-222222222222" \
  -H "X-User-Role: CUSTOMER"
```

## 6. Webhook (gateway callback)
`POST /api/v1/payments/webhook` is **not** protected by JWT — it's how the
real gateway (Stripe/PayHere) would asynchronously confirm a charge. It's
authenticated with an HMAC-SHA256 signature over the raw request body,
computed with `app.payment.webhook-secret` (env var `PAYMENT_WEBHOOK_SECRET`).

```bash
BODY='{"gatewayReference":"mock_xxx","status":"SUCCEEDED"}'
SECRET='dev-only-webhook-secret-change-me'
SIG=$(printf '%s' "$BODY" | openssl dgst -sha256 -hmac "$SECRET" | sed 's/^.* //')

curl -X POST http://localhost:8085/api/v1/payments/webhook \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Signature: $SIG" \
  -d "$BODY"
```

With the mock gateway, `POST /charge` already resolves the outcome
synchronously, so the webhook path mainly exists so the shape is ready for
a real async gateway later — it re-confirms/corrects a transaction found by
`gatewayReference`.

## 7. Events published
| Event | Routing key | Consumed by |
|---|---|---|
| `PaymentProcessed` | `payment.processed` | Notification Service (wired), Hotel/Restaurant Booking (**not yet wired** — see note below) |
| `PaymentFailed` | `payment.failed` | Same as above |

Both are published on the shared `stayseat.exchange` topic exchange (same
exchange `notification-service`'s queues are already bound to via
`payment.#`).

**Follow-up for whoever owns Hotel/Restaurant Booking Service:** per
`API_CONTRACT.md` §4.5, those services should subscribe to
`PaymentProcessed` and call their own `PATCH /bookings/{id}/confirm`
internally — Payment Service intentionally never calls them directly. As of
this commit, `hotel-booking-service` only has a `LoggingEventPublisher`
stub and no listener yet, so that wiring is still open work.

## 8. Swapping in a real gateway
`PaymentGatewayClient` is the seam — implement it again (e.g.
`StripePaymentGatewayClient`), annotate it `@Component`, and put
`MockPaymentGatewayClient` behind `@Profile("local-no-gateway")` so only one
bean exists at a time. Nothing in the controller/service/entity layers needs
to change.
