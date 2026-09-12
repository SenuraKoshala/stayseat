# StaySeat - Cloud-Based Unified Hotel Room and Restaurant Table Reservation System

A comprehensive microservices-based reservation platform that unifies hotel room bookings and restaurant table reservations into a single cloud-based system.

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Microservices](#microservices)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Event-Driven Architecture](#event-driven-architecture)
- [Security](#security)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## Overview

**StaySeat** is a cloud-based unified reservation system designed to streamline hotel room bookings and restaurant table reservations. Built using a microservices architecture, the system provides:

- Unified authentication and authorization
- Real-time availability checking
- Automated notification system
- Centralized user profile management
- Event-driven communication between services
- API Gateway for single entry point
- Rate limiting and security features

## System Architecture

The system follows a **microservices architecture** with the following key principles:

- **Database per Service**: Each microservice has its own PostgreSQL database
- **Event-Driven Communication**: Services communicate asynchronously via RabbitMQ
- **API Gateway Pattern**: Single entry point for all client requests
- **JWT-based Authentication**: Stateless authentication across all services
- **Service Independence**: Services can be developed, deployed, and scaled independently

### Architecture Diagram

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  API Gateway    │  (Port 8090)
│  Rate Limiting  │
│  CORS Config    │
└────────┬────────┘
         │
         ├────────────────┬────────────────┬──────────────┬──────────────┐
         ▼                ▼                ▼              ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────┐
│ Auth Service │  │ Hotel Booking│  │ Restaurant   │  │ User     │  │ Notif.   │
│  (Port 8081) │  │  (Port 8083) │  │ Booking      │  │ Service  │  │ Service  │
│              │  │              │  │ (Port 8084)  │  │(Port 8086)│  │(Port 8082)│
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └────┬─────┘  └────┬─────┘
       │                 │                 │               │             │
       ▼                 ▼                 ▼               ▼             ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  ┌──────────┐
│  Auth DB     │  │  Hotel DB    │  │Restaurant DB │  │ User DB  │  │ Notif DB │
│ (Port 5436)  │  │ (Port 5432)  │  │ (Port 5434)  │  │(Port 5437)│  │(Port 5435)│
└──────────────┘  └──────────────┘  └──────────────┘  └──────────┘  └──────────┘
                         │                 │
                         └────────┬────────┘
                                  ▼
                          ┌──────────────┐
                          │  RabbitMQ    │  (Port 5672)
                          │  Message     │  (UI: 15672)
                          │  Broker      │
                          └──────────────┘
```

## Microservices

### 1. API Gateway (`api-gateway`)
- **Port**: 8090
- **Purpose**: Single entry point for all client requests
- **Features**:
  - Request routing to appropriate microservices
  - JWT validation filter
  - Rate limiting (100 burst capacity, 50 requests/second per IP)
  - CORS configuration
  - Load balancing and fault tolerance

### 2. Authentication Service (`authservice`)
- **Port**: 8081
- **Database**: PostgreSQL (Port 5436)
- **Purpose**: User authentication and authorization
- **Features**:
  - User registration and login
  - JWT token generation (access & refresh tokens)
  - Token validation and refresh
  - Password encryption (BCrypt)
  - Role-based access control (USER, ADMIN, OWNER)
  - User registration event publishing

**Key Endpoints**:
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/validate` - Validate JWT token

### 3. Hotel Booking Service (`hotel-booking-service`)
- **Port**: 8083
- **Database**: PostgreSQL (Port 5432)
- **Purpose**: Hotel property and room booking management
- **Features**:
  - Property management (CRUD operations)
  - Room management with types (SINGLE, DOUBLE, SUITE, DELUXE)
  - Booking creation and management
  - Availability checking
  - Booking status tracking (PENDING, CONFIRMED, CANCELLED, COMPLETED)
  - Event publishing for booking confirmations
  - Flyway database migrations

**Key Endpoints**:
- `GET /api/v1/hotel/properties` - List all hotel properties
- `GET /api/v1/hotel/properties/{id}` - Get property details
- `POST /api/v1/hotel/properties` - Create property (OWNER role)
- `GET /api/v1/hotel/rooms` - List available rooms
- `POST /api/v1/hotel/bookings` - Create booking
- `GET /api/v1/hotel/bookings/{id}` - Get booking details

### 4. Restaurant Booking Service (`restaurant-booking-service`)
- **Port**: 8084
- **Database**: PostgreSQL (Port 5434)
- **Purpose**: Restaurant property and table reservation management
- **Features**:
  - Restaurant property management
  - Table management with capacity tracking
  - Table reservation system
  - Availability checking
  - Booking status management
  - Event publishing for reservations

**Key Endpoints**:
- `GET /api/v1/restaurant/properties` - List all restaurants
- `GET /api/v1/restaurant/properties/{id}` - Get restaurant details
- `POST /api/v1/restaurant/properties` - Create restaurant (OWNER role)
- `GET /api/v1/restaurant/tables` - List available tables
- `POST /api/v1/restaurant/bookings` - Create reservation
- `GET /api/v1/restaurant/bookings/{id}` - Get reservation details

### 5. User Service (`user-service`)
- **Port**: 8086
- **Database**: PostgreSQL (Port 5437)
- **Purpose**: User profile management
- **Features**:
  - User profile creation and updates
  - Profile retrieval
  - Listens to user registration events from Auth Service
  - Automatic profile creation on registration

**Key Endpoints**:
- `GET /api/v1/users/profile` - Get current user profile
- `PUT /api/v1/users/profile` - Update user profile
- `GET /api/v1/users/{userId}` - Get user by ID (internal)

### 6. Notification Service (`notification-service`)
- **Port**: 8082
- **Database**: PostgreSQL (Port 5435)
- **Purpose**: Event-driven notification system
- **Features**:
  - Email notifications via SMTP
  - Event listeners for:
    - Hotel booking confirmations
    - Restaurant booking confirmations
    - Payment processing events
    - Payment failure events
  - Notification logging and tracking
  - Status tracking (PENDING, SENT, FAILED)
  - Channel management (EMAIL, SMS, PUSH)

**Key Endpoints**:
- `GET /api/v1/notifications/logs` - Get notification history
- `GET /api/v1/notifications/logs/{id}` - Get specific notification log

## Technology Stack

### Backend Framework
- **Spring Boot** 3.3.4 (Hotel & Restaurant services)
- **Spring Boot** 4.0.7 (Notification service)
- **Spring Boot** 4.1.0 (Auth service)
- **Java** 17

### Frameworks & Libraries
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database ORM
- **Spring AMQP** - RabbitMQ integration
- **Hibernate** - ORM implementation
- **Flyway** - Database migrations

### Database
- **PostgreSQL** 16 - All microservices
- **Database-per-service** pattern

### Message Broker
- **RabbitMQ** 4 with Management UI
- Event-driven communication

### Security
- **JWT (JSON Web Tokens)** - Authentication
- **jjwt** 0.12.6 - JWT library
- **BCrypt** - Password hashing

### Documentation
- **SpringDoc OpenAPI** 2.6.0
- **Swagger UI** - API documentation

### Build Tool
- **Maven** - Dependency management & build
- Multi-module Maven project

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

### Additional Libraries
- **Lombok** - Boilerplate reduction
- **Jakarta Validation** - Input validation
- **PostgreSQL JDBC Driver**

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Git**
- **PostgreSQL** client (optional, for database access)
- **Postman** or similar tool for API testing (optional)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/stayseat.git
cd stayseat/backend
```

### 2. Start Infrastructure Services

Start PostgreSQL databases and RabbitMQ using Docker Compose:

```bash
docker compose -f backend/docker-compose.yml up -d
```

This will start:
- RabbitMQ (AMQP: 5672, Management UI: http://localhost:15672)
- Auth DB (Port 5436)
- Hotel DB (Port 5432)
- Restaurant DB (Port 5434)
- Notification DB (Port 5435)
- User DB (Port 5437)

**RabbitMQ Management UI**: http://localhost:15672 (guest/guest)

### 3. Build All Services

From the `backend` directory:

```bash
mvn clean install
```

Or build individual services:

```bash
mvn -pl authservice clean install
mvn -pl hotel-booking-service clean install
mvn -pl restaurant-booking-service clean install
mvn -pl notification-service clean install
mvn -pl user-service clean install
mvn -pl api-gateway clean install
```

### 4. Run Services

**Option A: Run from IDE**
- Import the project into IntelliJ IDEA or Eclipse
- Run each `*Application.java` file as a Spring Boot application

**Option B: Run with Maven**

```bash
# Terminal 1 - Auth Service
mvn -pl authservice spring-boot:run

# Terminal 2 - User Service
mvn -pl user-service spring-boot:run

# Terminal 3 - Hotel Booking Service
mvn -pl hotel-booking-service spring-boot:run

# Terminal 4 - Restaurant Booking Service
mvn -pl restaurant-booking-service spring-boot:run

# Terminal 5 - Notification Service
mvn -pl notification-service spring-boot:run

# Terminal 6 - API Gateway
mvn -pl api-gateway spring-boot:run
```

### 5. Verify Services

Check that all services are running:

```bash
curl http://localhost:8090/api/v1/auth/validate  # API Gateway routing to Auth
curl http://localhost:8081/actuator/health       # Auth Service
curl http://localhost:8083/actuator/health       # Hotel Service
curl http://localhost:8084/actuator/health       # Restaurant Service
curl http://localhost:8086/actuator/health       # User Service
curl http://localhost:8082/actuator/health       # Notification Service
```

## Configuration

### Environment Variables

Create a `.env` file in the `backend` directory (optional):

```properties
# Database Configuration
AUTH_DB_HOST=localhost
AUTH_DB_PORT=5436
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_svc
AUTH_DB_PASSWORD=auth_pass

HOTEL_DB_URL=jdbc:postgresql://localhost:5432/hotel_booking
HOTEL_DB_USER=hotel_svc
HOTEL_DB_PASSWORD=hotel_pass

RESTAURANT_DB_URL=jdbc:postgresql://localhost:5434/restaurant_booking
RESTAURANT_DB_USER=restaurant_svc
RESTAURANT_DB_PASSWORD=restaurant_pass

NOTIF_DB_USER=postgres
NOTIF_DB_PASSWORD=admin

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# JWT Configuration
JWT_SECRET=your-secret-key-here-change-in-production-must-be-same-across-all-services

# Gateway Configuration
GATEWAY_PORT=8090

# Service URLs
AUTH_SERVICE_URI=http://localhost:8081
HOTEL_SERVICE_URI=http://localhost:8083
RESTAURANT_SERVICE_URI=http://localhost:8084
USER_SERVICE_URI=http://localhost:8086
NOTIFICATION_SERVICE_URI=http://localhost:8082

# Email Configuration (for Notification Service)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

### JWT Configuration

**IMPORTANT**: All services must use the **same JWT secret** for token validation to work correctly.

- Default: `dev-only-placeholder-secret-change-me-before-integrating-with-auth-service`
- For production, set `JWT_SECRET` environment variable to a strong, random secret

### Token Expiry

- **Access Token**: 15 minutes (900,000 ms)
- **Refresh Token**: 7 days (604,800,000 ms)

## API Documentation

### Swagger UI

Each service exposes its own Swagger UI:

- **Auth Service**: http://localhost:8081/swagger-ui.html
- **Hotel Booking**: http://localhost:8083/swagger-ui.html
- **Restaurant Booking**: http://localhost:8084/swagger-ui.html
- **User Service**: http://localhost:8086/swagger-ui.html
- **Notification Service**: http://localhost:8082/swagger-ui.html

### API Gateway Routes

All client requests should go through the API Gateway at `http://localhost:8090`:

| Service | Gateway Path | Backend Service |
|---------|-------------|-----------------|
| Auth | `/api/v1/auth/**` | http://localhost:8081 |
| Hotel | `/api/v1/hotel/**` | http://localhost:8083 |
| Restaurant | `/api/v1/restaurant/**` | http://localhost:8084 |
| User | `/api/v1/users/**` | http://localhost:8086 |
| Notification | `/api/v1/notifications/**` | http://localhost:8082 |

### Authentication Flow

1. **Register**: `POST /api/v1/auth/register`
   ```json
   {
     "name": "John Doe",
     "email": "john@example.com",
     "password": "SecurePass123",
     "role": "USER"
   }
   ```

2. **Login**: `POST /api/v1/auth/login`
   ```json
   {
     "email": "john@example.com",
     "password": "SecurePass123"
   }
   ```

   Response:
   ```json
   {
     "accessToken": "eyJhbGc...",
     "refreshToken": "eyJhbGc...",
     "tokenType": "Bearer"
   }
   ```

3. **Use Token**: Include in Authorization header
   ```
   Authorization: Bearer <accessToken>
   ```

4. **Refresh Token**: `POST /api/v1/auth/refresh`
   ```json
   {
     "refreshToken": "eyJhbGc..."
   }
   ```

## Database Schema

### Auth Service (`auth_db`)

**Table: auth_users**
```sql
- id (UUID, PK)
- email (VARCHAR, UNIQUE)
- password (VARCHAR, encrypted)
- role (VARCHAR: USER, ADMIN, OWNER)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Hotel Booking Service (`hotel_booking`)

**Table: properties**
```sql
- id (BIGINT, PK)
- name (VARCHAR)
- description (TEXT)
- location (VARCHAR)
- owner_id (UUID)
- created_at (TIMESTAMP)
```

**Table: rooms**
```sql
- id (BIGINT, PK)
- property_id (BIGINT, FK)
- room_number (VARCHAR)
- room_type (ENUM: SINGLE, DOUBLE, SUITE, DELUXE)
- price_amount (DECIMAL)
- price_currency (VARCHAR)
- is_available (BOOLEAN)
```

**Table: hotel_bookings**
```sql
- id (BIGINT, PK)
- room_id (BIGINT, FK)
- user_id (UUID)
- check_in_date (DATE)
- check_out_date (DATE)
- guest_count (INTEGER)
- total_amount (DECIMAL)
- status (ENUM: PENDING, CONFIRMED, CANCELLED, COMPLETED)
- created_at (TIMESTAMP)
```

### Restaurant Booking Service (`restaurant_booking`)

**Table: restaurant_properties**
```sql
- id (BIGINT, PK)
- name (VARCHAR)
- description (TEXT)
- location (VARCHAR)
- cuisine_type (VARCHAR)
- owner_id (UUID)
- created_at (TIMESTAMP)
```

**Table: restaurant_tables**
```sql
- id (BIGINT, PK)
- property_id (BIGINT, FK)
- table_number (VARCHAR)
- capacity (INTEGER)
- is_available (BOOLEAN)
```

**Table: restaurant_bookings**
```sql
- id (BIGINT, PK)
- table_id (BIGINT, FK)
- user_id (UUID)
- reservation_date (DATE)
- reservation_time (TIME)
- party_size (INTEGER)
- status (ENUM: PENDING, CONFIRMED, CANCELLED, COMPLETED)
- created_at (TIMESTAMP)
```

### User Service (`user_db`)

**Table: user_profiles**
```sql
- id (UUID, PK)
- user_id (UUID, UNIQUE)
- first_name (VARCHAR)
- last_name (VARCHAR)
- phone_number (VARCHAR)
- address (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Notification Service (`notification_db`)

**Table: notification_logs**
```sql
- id (BIGINT, PK)
- user_id (UUID)
- recipient (VARCHAR)
- subject (VARCHAR)
- message (TEXT)
- channel (ENUM: EMAIL, SMS, PUSH)
- status (ENUM: PENDING, SENT, FAILED)
- error_message (TEXT)
- sent_at (TIMESTAMP)
- created_at (TIMESTAMP)
```

## Event-Driven Architecture

The system uses **RabbitMQ** for asynchronous event-driven communication.

### Published Events

#### Auth Service → User Service
- **Event**: `user.registered`
- **Exchange**: `user.events`
- **Routing Key**: `user.registered`
- **Payload**: User registration details (userId, email, name)

#### Hotel Booking Service → Notification Service
- **Event**: `hotel.booking.confirmed`
- **Exchange**: `booking.events`
- **Routing Key**: `hotel.booking.confirmed`
- **Payload**: Booking details (bookingId, userId, propertyName, dates)

#### Restaurant Booking Service → Notification Service
- **Event**: `restaurant.booking.confirmed`
- **Exchange**: `booking.events`
- **Routing Key**: `restaurant.booking.confirmed`
- **Payload**: Reservation details (bookingId, userId, restaurantName, dateTime)

#### Payment Events (Placeholder)
- **Event**: `payment.processed`
- **Event**: `payment.failed`
- **Exchange**: `payment.events`

### Event Flow Example

```
User Registration Flow:
1. Client → Auth Service: POST /register
2. Auth Service → Database: Create user
3. Auth Service → RabbitMQ: Publish user.registered event
4. User Service ← RabbitMQ: Consume event
5. User Service → Database: Create user profile

Hotel Booking Flow:
1. Client → Hotel Service: POST /bookings
2. Hotel Service → Database: Create booking
3. Hotel Service → RabbitMQ: Publish hotel.booking.confirmed
4. Notification Service ← RabbitMQ: Consume event
5. Notification Service → SMTP: Send confirmation email
6. Notification Service → Database: Log notification
```

## Security

### Authentication & Authorization

- **JWT-based** authentication (stateless)
- **Role-based** access control (RBAC)
- **Password encryption** using BCrypt
- **Token expiration** and refresh mechanism

### Roles

- **USER**: Can create bookings/reservations, manage own profile
- **OWNER**: Can create and manage properties (hotels/restaurants)
- **ADMIN**: Full system access (future implementation)

### Security Configuration

- JWT validation on all protected endpoints
- CORS configuration in API Gateway
- Rate limiting (100 requests burst, 50/second sustained)
- Stateless session management
- CSRF protection disabled (JWT-based API)

### Protected Endpoints

Most endpoints require authentication. Public endpoints:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`

## Project Structure

```
backend/
├── api-gateway/                 # API Gateway service
│   ├── src/main/java/
│   │   └── com/stayseat/gateway/
│   │       ├── config/          # CORS, Gateway routes
│   │       └── filter/          # JWT auth, Rate limiting
│   └── src/main/resources/
│       └── application.yml
│
├── authservice/                 # Authentication service
│   ├── src/main/java/
│   │   └── com/senura/authservice/
│   │       ├── config/          # Security, Password encoder
│   │       ├── controller/      # Auth endpoints
│   │       ├── dto/             # Request/Response DTOs
│   │       ├── entity/          # AuthUser entity
│   │       ├── repository/      # JPA repositories
│   │       ├── security/        # JWT utilities
│   │       ├── service/         # Business logic
│   │       ├── messaging/       # RabbitMQ publishers
│   │       └── exception/       # Exception handlers
│   └── src/main/resources/
│       └── application.properties
│
├── hotel-booking-service/       # Hotel booking service
│   ├── src/main/java/
│   │   └── com/stayseat/hotelbooking/
│   │       ├── config/          # Security, JWT filter
│   │       ├── controller/      # REST controllers
│   │       ├── dto/             # DTOs
│   │       ├── entity/          # JPA entities
│   │       ├── event/           # Domain events
│   │       ├── repository/      # Repositories
│   │       └── service/         # Services
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/        # Flyway migrations
│
├── restaurant-booking-service/  # Restaurant booking service
│   ├── src/main/java/
│   │   └── com/stayseat/restaurantbooking/
│   │       ├── config/          # Security, JWT filter
│   │       ├── controller/      # REST controllers
│   │       ├── dto/             # DTOs
│   │       ├── entity/          # JPA entities
│   │       ├── event/           # Domain events
│   │       ├── repository/      # Repositories
│   │       └── service/         # Services
│   └── src/main/resources/
│       └── application.yml
│
├── user-service/                # User profile service
│   ├── src/main/java/
│   │   └── com/stayseat/userservice/
│   │       ├── config/          # Configuration
│   │       ├── controller/      # User endpoints
│   │       ├── dto/             # DTOs
│   │       ├── entity/          # UserProfile entity
│   │       ├── listener/        # Event listeners
│   │       ├── repository/      # Repositories
│   │       └── service/         # Services
│   └── src/main/resources/
│       └── application.yml
│
├── notification-service/        # Notification service
│   ├── src/main/java/
│   │   └── com/stayseat/notification_service/
│   │       ├── client/          # Inter-service clients
│   │       ├── config/          # RabbitMQ config
│   │       ├── controller/      # Notification logs API
│   │       ├── dto/             # DTOs
│   │       ├── entity/          # NotificationLog entity
│   │       ├── event/           # Event payloads
│   │       ├── listener/        # RabbitMQ listeners
│   │       ├── repository/      # Repositories
│   │       └── service/         # Email service, processors
│   └── src/main/resources/
│       └── application.properties
│
├── infrastructure/
│   └── docker-compose.yml       # Alternative compose location
│
├── docker-compose.yml           # Main infrastructure setup
├── pom.xml                      # Multi-module Maven config
└── README.md                    # This file
```

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
mvn -pl hotel-booking-service test
```

### Database Migrations (Flyway)

Hotel and Restaurant services use Flyway for database versioning.

Migrations location: `src/main/resources/db/migration/`

```bash
# Apply migrations
mvn -pl hotel-booking-service flyway:migrate

# Check migration status
mvn -pl hotel-booking-service flyway:info

# Repair migration history (if needed)
mvn -pl hotel-booking-service flyway:repair
```

### Adding a New Service

1. Create new Maven module in `backend/pom.xml`
2. Add service-specific database in `docker-compose.yml`
3. Configure application properties
4. Implement controllers, services, entities
5. Add routes in API Gateway configuration
6. Update this README

### Code Style

- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Implement proper exception handling
- Add validation annotations on DTOs
- Document public APIs with JavaDoc
- Use meaningful variable and method names

## Testing

### Manual Testing with Postman

1. Import API collection from `postman/` directory (if available)
2. Start all services
3. Register a new user
4. Login to get JWT token
5. Use token in Authorization header for protected endpoints

### Example API Call Flow

```bash
# 1. Register user
curl -X POST http://localhost:8090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "USER"
  }'

# 2. Login
curl -X POST http://localhost:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# 3. Get hotel properties (authenticated)
curl -X GET http://localhost:8090/api/v1/hotel/properties \
  -H "Authorization: Bearer <your-access-token>"

# 4. Create hotel booking
curl -X POST http://localhost:8090/api/v1/hotel/bookings \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-10-01",
    "checkOutDate": "2026-10-05",
    "guestCount": 2
  }'
```

## Deployment

### Docker Deployment

1. Build Docker images for each service
2. Update `docker-compose.yml` to include application containers
3. Configure environment variables
4. Run `docker compose up -d`

### Cloud Deployment Considerations

- Use managed PostgreSQL (AWS RDS, Google Cloud SQL)
- Use managed message broker (AWS MQ, Google Cloud Pub/Sub)
- Configure API Gateway with load balancer
- Set up auto-scaling for services
- Use secrets management (AWS Secrets Manager, Vault)
- Configure monitoring and logging (ELK stack, CloudWatch)
- Set up CI/CD pipeline (GitHub Actions, Jenkins)

### Environment-Specific Configuration

- **Development**: `.env` file with local settings
- **Staging**: Environment variables in staging environment
- **Production**: Secure secrets management, no defaults

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines

- Follow existing code style
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact & Support

For questions, issues, or contributions:
- Create an issue in the GitHub repository
- Contact the development team

## Roadmap

### Planned Features

- [ ] Payment Service integration
- [ ] Email template system for notifications
- [ ] SMS notifications
- [ ] Push notifications (mobile)
- [ ] Advanced search and filtering
- [ ] Reviews and ratings system
- [ ] Loyalty program
- [ ] Analytics dashboard
- [ ] Admin panel
- [ ] Mobile app integration
- [ ] Kubernetes deployment manifests
- [ ] Comprehensive test coverage
- [ ] Performance monitoring
- [ ] Caching layer (Redis)

---

**StaySeat** - Simplifying reservations, one booking at a time.
