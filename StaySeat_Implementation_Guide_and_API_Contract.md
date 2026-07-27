# StaySeat — Implementation Guide & API Contract
### Cloud-Based Unified Hotel Room and Restaurant Table Reservation System
Based on CA01 (Software Architecture, EC8208) — for use during CA03 prototype implementation.

---

## 1. Why this document exists

Your CA01 report defines *what* the services are. It doesn't yet define the exact
request/response shapes, field names, ID formats, or error conventions each service
must use. When 5 people build 5 services in parallel, **that's where integration
breaks** — one person uses `roomId` as an int, another expects a UUID string; one
returns `{ "data": {...} }`, another returns the object directly. This doc fixes
those decisions up front so nobody has to guess.

Treat Section 3 (Shared Conventions) as **non-negotiable** — everyone follows it
exactly. Treat Sections 4.x (per-service specs) as the working contract each owner
implements against; changes to a service's contract should be proposed as an edit
to this file (PR) so the rest of the team sees it.

---

## 2. Recommended Build Order

Building all 6 services at once from day one usually stalls because Hotel/Restaurant/
Payment/Notification all depend on Auth existing first, and nobody can test an
integrated flow until the Gateway routes traffic. Build in these phases instead:

| Phase | What | Who | Notes |
|---|---|---|---|
| **0. Setup (Week 1)** | Shared repo structure (mono-repo or one repo per service + a `contracts` repo), Docker Compose with Postgres instances + Kafka/RabbitMQ (local broker substitute for SNS/SQS), shared `.env.example`, this document finalized and agreed | Whole team, 1 session | Do this together, live, not solo |
| **1. Foundational (Week 2–3)** | **Auth Service** + **User Service** | 2 members | Everything else needs a valid JWT and a user record to reference. Senura's existing auth-service scaffold plugs in here. |
| **2. Domain services (Week 3–5, parallel)** | **Hotel Booking Service** and **Restaurant Booking Service** built in parallel — they don't depend on each other | 2 members (1 each) | Both only need Auth's JWT validation + this contract. Use mocked/stubbed JWT in the interim if Auth isn't fully done yet. |
| **3. Payment Service (Week 4–5)** | Can start once booking services have a stable `POST /bookings` response shape (payment needs `bookingId`, `amount`) | 1 member | Can be built against a mocked booking-created payload before Hotel/Restaurant are fully done. |
| **4. Notification Service (Week 5–6)** | Consumes events from all others via the message broker | Whichever member finishes their Phase 1/2 service first | Lowest external dependency — good "catch-up" task if someone falls behind. |
| **5. API Gateway wiring (Week 6)** | Route table, JWT validation filter, rate limiting | 1 member (can be the Auth owner, since they know the JWT format best) | Do this once real services exist — don't over-build the gateway before there's anything to route to. |
| **6. Frontend integration (Week 6–7)** | React/Vite app wired to the Gateway | Whole team or 1–2 dedicated | Only start serious frontend work once Section 4 contracts are stable — this is the #1 source of late rework. |
| **7. Hardening (Week 7–8)** | Error handling, validation, retry/idempotency on payment, seed data, deployment to AWS/Docker, demo prep | Whole team | |

**Suggested ownership split (5 members)** — map these to your actual names:
- **Member A:** API Gateway + Auth Service
- **Member B:** User Service
- **Member C:** Hotel Booking Service
- **Member D:** Restaurant Booking Service
- **Member E:** Payment Service + Notification Service

Each owner is responsible for keeping their section of Section 4 accurate as they build.

---

## 3. Shared Conventions (apply to every service)

### 3.1 Base routing (via API Gateway)
| Service | Path prefix |
|---|---|
| Auth Service | `/api/v1/auth` |
| User Service | `/api/v1/users` |
| Hotel Booking Service | `/api/v1/hotel` |
| Restaurant Booking Service | `/api/v1/restaurant` |
| Payment Service | `/api/v1/payments` |
| Notification Service | `/api/v1/notifications` |

### 3.2 ID format
**Use UUID (string) for every entity ID that crosses a service boundary** — `userId`,
`bookingId`, `propertyId`, `roomId`, `paymentId`, etc. Internal auto-increment DB
primary keys are fine *inside* a service's own database, but anything referenced by
another service or returned in an API response must be a UUID string. This avoids
collisions and guessable sequential IDs across independently-owned databases.

### 3.3 Date/time and money
- All timestamps: **ISO 8601, UTC**, e.g. `"2026-08-10T14:30:00Z"`.
- Date-only fields (e.g. check-in date): `"2026-08-10"`.
- Money: always an object, never a bare number —
  ```json
  { "amount": 4500.00, "currency": "LKR" }
  ```

### 3.4 Standard success response envelope
```json
{
  "success": true,
  "data": { },
  "meta": { }
}
```
`meta` is optional (used for pagination). `data` holds the resource or array.

### 3.5 Standard error response envelope
```json
{
  "success": false,
  "error": {
    "code": "ROOM_NOT_AVAILABLE",
    "message": "The selected room is not available for the given dates.",
    "details": { }
  }
}
```
Use HTTP status codes correctly (`400` validation, `401` unauthenticated, `403`
forbidden, `404` not found, `409` conflict e.g. double-booking, `500` server error).
Define an `error.code` enum per service — list yours in that service's section below
as you build it.

### 3.6 Pagination (for list endpoints)
Query params: `?page=0&size=20&sort=createdAt,desc`
```json
{
  "success": true,
  "data": [ ],
  "meta": { "page": 0, "size": 20, "totalElements": 143, "totalPages": 8 }
}
```

### 3.7 Auth header
Every authenticated request:
```
Authorization: Bearer <jwt>
```
JWT claims (agree on this exact shape — Auth Service issues it, every other service
verifies it, Gateway does the first-line check):
```json
{
  "sub": "userId (UUID)",
  "email": "user@example.com",
  "role": "CUSTOMER | HOTEL_ADMIN | RESTAURANT_ADMIN | SYSTEM_ADMIN",
  "iat": 1731234567,
  "exp": 1731238167
}
```

### 3.8 JSON field naming
`camelCase` everywhere, no exceptions. No snake_case sneaking in from a Postgres
column mapped 1:1.

### 3.9 Message broker (async events)
Local dev: RabbitMQ or Kafka (whichever the team is more comfortable with) standing
in for Amazon SNS/SQS. Every event follows this envelope:
```json
{
  "eventId": "UUID",
  "eventType": "HotelBookingConfirmed",
  "occurredAt": "2026-08-10T14:30:00Z",
  "payload": { }
}
```
Full topic/event list is in Section 5.

---

## 4. Per-Service API Contracts

### 4.1 Auth Service
**Owns:** `users_auth` table (credentials only — NOT profile data, that's User Service).

**Entity: `AuthUser`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | PK |
| email | string | unique |
| passwordHash | string | BCrypt, never returned in API |
| role | enum | CUSTOMER, HOTEL_ADMIN, RESTAURANT_ADMIN, SYSTEM_ADMIN |
| isVerified | boolean | |
| createdAt | datetime | |

**Endpoints**
| Method | Path | Body | Response | Auth |
|---|---|---|---|---|
| POST | `/register` | `{ email, password, role }` | `201` → `{ userId, email, role }` | none |
| POST | `/login` | `{ email, password }` | `200` → `{ accessToken, refreshToken, expiresIn }` | none |
| POST | `/refresh` | `{ refreshToken }` | `200` → `{ accessToken, expiresIn }` | none |
| POST | `/logout` | `{ refreshToken }` | `204` | Bearer |
| GET | `/validate` | — (token in header) | `200` → `{ valid, userId, role }` | Bearer — **internal, gateway-only** |

**Publishes event:** `UserRegistered` → `{ userId, email, role }` (User Service consumes this to create the profile row)

---

### 4.2 User Service
**Owns:** `user_profiles` table.

**Entity: `UserProfile`**
| Field | Type | Notes |
|---|---|---|
| userId | UUID | PK, = AuthUser.id |
| firstName | string | |
| lastName | string | |
| phone | string | |
| imageUrl | string | nullable, S3 URL |
| role | enum | denormalized copy from Auth for convenience |
| createdAt | datetime | |

**Endpoints**
| Method | Path | Body | Response | Auth |
|---|---|---|---|---|
| GET | `/me` | — | `200` → `UserProfile` | Bearer |
| GET | `/{id}` | — | `200` → `UserProfile` (public subset) | Bearer |
| PUT | `/me` | `{ firstName, lastName, phone }` | `200` → `UserProfile` | Bearer |
| POST | `/me/image` | multipart file | `200` → `{ imageUrl }` | Bearer |

**Consumes event:** `UserRegistered` (creates the profile stub row)

---

### 4.3 Hotel Booking Service
**Owns:** `properties`, `rooms`, `room_availability`, `hotel_bookings`.

**Entity: `Room`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| propertyId | UUID | |
| roomNumber | string | |
| type | enum | SINGLE, DOUBLE, SUITE, DELUXE |
| capacity | int | |
| pricePerNight | Money | see §3.3 |

**Entity: `HotelBooking`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| roomId | UUID | |
| customerId | UUID | = AuthUser.id |
| checkInDate | date | |
| checkOutDate | date | |
| status | enum | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| totalAmount | Money | |
| createdAt | datetime | |

**Endpoints**
| Method | Path | Body / Query | Response | Auth |
|---|---|---|---|---|
| GET | `/properties` | `?city=` | `200` → list of properties | none |
| POST | `/properties` | property payload | `201` | HOTEL_ADMIN |
| GET | `/properties/{propertyId}/rooms` | — | `200` → list of Room | none |
| POST | `/rooms` | Room payload | `201` | HOTEL_ADMIN |
| GET | `/availability` | `?propertyId=&checkIn=&checkOut=` | `200` → list of available Room | none |
| POST | `/bookings` | `{ roomId, checkInDate, checkOutDate }` | `201` → `HotelBooking` (status `PENDING`) | CUSTOMER |
| GET | `/bookings/{id}` | — | `200` → `HotelBooking` | owner or admin |
| GET | `/bookings/me` | — (paginated) | `200` → list of HotelBooking | CUSTOMER |
| PATCH | `/bookings/{id}/confirm` | — | `200` → `HotelBooking` (status `CONFIRMED`) | HOTEL_ADMIN, **or internal call from Payment Service on successful charge** |
| PATCH | `/bookings/{id}/cancel` | `{ reason }` | `200` → `HotelBooking` (status `CANCELLED`) | owner or admin |

**Double-booking prevention:** availability check + booking insert must happen inside
one DB transaction with a unique constraint on `(roomId, dateRange)` — don't rely on
the application-layer check alone under concurrent load.

**Publishes events:** `HotelBookingCreated`, `HotelBookingConfirmed`, `HotelBookingCancelled`
→ payload: `{ bookingId, customerId, roomId, checkInDate, checkOutDate, totalAmount }`

---

### 4.4 Restaurant Booking Service
**Owns:** `restaurant_properties`, `tables`, `restaurant_bookings`.

**Entity: `RestaurantBooking`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| tableId | UUID | |
| customerId | UUID | |
| reservationDate | date | |
| timeSlot | string | e.g. `"19:30"` |
| partySize | int | |
| status | enum | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| createdAt | datetime | |

**Endpoints** (mirrors Hotel Booking Service's pattern)
| Method | Path | Body / Query | Response | Auth |
|---|---|---|---|---|
| GET | `/properties` | `?city=` | `200` → list | none |
| GET | `/properties/{propertyId}/tables` | — | `200` → list of Table | none |
| GET | `/availability` | `?propertyId=&date=&timeSlot=&partySize=` | `200` → list of available Table | none |
| POST | `/bookings` | `{ tableId, reservationDate, timeSlot, partySize }` | `201` → `RestaurantBooking` | CUSTOMER |
| GET | `/bookings/{id}` | — | `200` | owner or admin |
| GET | `/bookings/me` | — | `200` list | CUSTOMER |
| PATCH | `/bookings/{id}/confirm` | — | `200` | RESTAURANT_ADMIN |
| PATCH | `/bookings/{id}/cancel` | `{ reason }` | `200` | owner or admin |

**Publishes events:** `RestaurantBookingCreated`, `RestaurantBookingConfirmed`, `RestaurantBookingCancelled`
→ payload: `{ bookingId, customerId, tableId, reservationDate, timeSlot, partySize }`

---

### 4.5 Payment Service
**Owns:** `transactions`.

**Entity: `Transaction`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| bookingId | UUID | references either a hotel or restaurant booking |
| bookingType | enum | HOTEL, RESTAURANT |
| customerId | UUID | |
| amount | Money | |
| status | enum | PENDING, SUCCEEDED, FAILED, REFUNDED |
| gatewayReference | string | Stripe/PayHere transaction ID |
| createdAt | datetime | |

**Endpoints**
| Method | Path | Body | Response | Auth |
|---|---|---|---|---|
| POST | `/charge` | `{ bookingId, bookingType, amount }` | `201` → `Transaction` | CUSTOMER |
| GET | `/{id}` | — | `200` → `Transaction` | owner or admin |
| GET | `/booking/{bookingId}` | — | `200` → `Transaction` | owner or admin |
| POST | `/webhook` | gateway-specific payload | `200` | **none — verify via gateway signature, not JWT** |

**Flow note:** `POST /charge` calls the external gateway synchronously, persists the
`Transaction`, then **publishes** `PaymentProcessed` (or `PaymentFailed`). It does
**not** call the Hotel/Restaurant service directly — those services should each
subscribe to `PaymentProcessed` and call their own `confirm` transition internally.
This keeps Payment Service decoupled from booking-domain logic.

**Publishes events:** `PaymentProcessed`, `PaymentFailed`
→ payload: `{ transactionId, bookingId, bookingType, customerId, amount, status }`

---

### 4.6 Notification Service
**Owns:** `notification_logs`. No public write endpoints — this service is almost
entirely event-driven.

**Entity: `NotificationLog`**
| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| userId | UUID | |
| channel | enum | EMAIL, SMS |
| type | string | e.g. `BOOKING_CONFIRMATION` |
| status | enum | SENT, FAILED |
| sentAt | datetime | |

**Endpoints**
| Method | Path | Response | Auth |
|---|---|---|---|
| GET | `/logs?userId=` | list of NotificationLog | SYSTEM_ADMIN |

**Consumes events:** `HotelBookingConfirmed`, `RestaurantBookingConfirmed`,
`PaymentProcessed`, `PaymentFailed` → renders template, sends via Amazon SES/SNS,
writes a `NotificationLog` row.

---

### 4.7 API Gateway
No business entities. Responsibilities only:
1. Route `/api/v1/<service>/**` to the correct backend service.
2. On every request except `/auth/register`, `/auth/login`, `/auth/refresh`, and
   `/payments/webhook` — validate the JWT (either locally by checking signature +
   expiry, or by calling Auth's `/validate`) and reject with `401` before forwarding.
3. Attach `X-User-Id` and `X-User-Role` headers to the forwarded request so
   downstream services don't need to re-parse the JWT themselves (they should still
   verify signature if they want defense-in-depth, but role-based authorization
   logic can read these headers).
4. Rate limiting per IP/user (simple bucket, doesn't need to be fancy for CA03).

---

## 5. Event / Topic Summary

| Event | Producer | Consumer(s) | Key payload fields |
|---|---|---|---|
| `UserRegistered` | Auth Service | User Service | userId, email, role |
| `HotelBookingCreated` | Hotel Booking | (optional: analytics) | bookingId, customerId, roomId, totalAmount |
| `HotelBookingConfirmed` | Hotel Booking | Notification Service | bookingId, customerId, checkInDate, checkOutDate |
| `HotelBookingCancelled` | Hotel Booking | Notification Service | bookingId, customerId, reason |
| `RestaurantBookingCreated` | Restaurant Booking | (optional) | bookingId, customerId, tableId |
| `RestaurantBookingConfirmed` | Restaurant Booking | Notification Service | bookingId, customerId, reservationDate, timeSlot |
| `RestaurantBookingCancelled` | Restaurant Booking | Notification Service | bookingId, customerId, reason |
| `PaymentProcessed` | Payment Service | Hotel Booking, Restaurant Booking, Notification | transactionId, bookingId, bookingType, status |
| `PaymentFailed` | Payment Service | Hotel Booking, Restaurant Booking, Notification | transactionId, bookingId, bookingType, status |

Use one topic/queue per event type (or one topic with `eventType` filtering if your
broker setup is simpler that way) — agree this as a team before Phase 2 starts, since
both booking services need to consume `PaymentProcessed`.

---

## 6. Before You Start Coding — Team Checklist

- [ ] Everyone has read this doc and agrees on Section 3 (no silent deviations)
- [ ] Repo structure decided (mono-repo vs multi-repo) and pushed
- [ ] `docker-compose.yml` with Postgres ×5 (or ×1 with 5 schemas) + broker running locally for everyone
- [ ] Auth Service owner shares a **working JWT example** (real token) with the team by end of Phase 1, so others can test against it before Auth is 100% done
- [ ] Each service owner adds their actual `error.code` values to Section 3.5 as they build validation
- [ ] Postman/Insomnia collection (or OpenAPI/Swagger spec generated from Spring annotations) shared in the repo so nobody hand-writes requests from memory
- [ ] Weekly 15-min sync specifically to catch contract drift (this document should be the single source of truth — if a service's real behavior diverges from it, fix the doc, not just the code)

---

## 7. Suggested Next Steps

1. Generate OpenAPI/Swagger specs per service (springdoc-openapi is a one-dependency
   add for Spring Boot) so this contract is enforced in code, not just on paper.
2. Once Auth + User services are stable, write a shared Postman collection with
   environment variables for `{{baseUrl}}` and `{{accessToken}}` so every member
   tests against the same requests.
3. Keep this file in the repo root as `API_CONTRACT.md` and update it via PR
   whenever a service's contract changes — don't let it go stale.
