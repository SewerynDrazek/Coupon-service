# Coupon Service

REST service for managing discount coupons. Supports creating coupons and registering their use, with country-based geo restrictions enforced via IP lookup and per-user limits enforced under concurrent load.

## Prerequisites

- Docker and Docker Compose

## Running

```bash
docker-compose up
```

The service starts on `http://localhost:8080`.

| Resource        | URL                                      |
|-----------------|------------------------------------------|
| Swagger UI      | http://localhost:8080/swagger-ui.html    |
| OpenAPI JSON    | http://localhost:8080/api-docs           |
| Health check    | http://localhost:8080/actuator/health    |

## API

### Create a coupon

```
POST /api/v1/coupons
Content-Type: application/json

{
  "code": "SUMMER10",
  "volume": 100,
  "country": "PL"
}
```

- `code` — unique, case-insensitive (`SUMMER10` and `summer10` are the same coupon)
- `volume` — maximum number of uses
- `country` — ISO 3166-1 alpha-2 country code; restricts usage to requests from that country

**Response `201 Created`:**
```json
{
  "code": "SUMMER10",
  "createdDate": "2025-01-01",
  "volume": 100,
  "spent": 0,
  "remaining": 100,
  "country": "PL"
}
```

### Use a coupon

```
POST /api/v1/coupons/{code}/use
Content-Type: application/json

{
  "userId": "user-123"
}
```

The country of the caller is resolved from their IP address. Each user may use a given coupon only once.

**Response `200 OK`:**
```json
{
  "code": "SUMMER10",
  "createdDate": "2025-01-01",
  "volume": 100,
  "spent": 1,
  "remaining": 99,
  "country": "PL"
}
```

**Error responses:**

| Status | Condition                                  |
|--------|--------------------------------------------|
| 400    | Invalid request body                       |
| 403    | Request IP resolves to a different country |
| 404    | Coupon code not found                      |
| 409    | Coupon exhausted or already used by user   |
| 503    | Geo-location service unavailable           |

## Running tests

Tests are self-contained — Testcontainers spins up PostgreSQL automatically.

```bash
./mvnw test
```

## Architecture

Hexagonal (ports & adapters) architecture:

```
api/                  — inbound adapter: HTTP controllers, DTOs, validation
domain/
  model/              — Coupon aggregate, value objects (Code, Volume, UserId)
  port/               — outbound port interfaces (CouponRepository, GeoLocationPort, ...)
  service/            — application logic (CouponService)
  exception/          — domain exceptions
infrastructure/
  persistence/        — JPA adapters implementing domain repository ports
  geolocation/        — HTTP adapter for ip-api.com
```

Key design decisions are documented in [docs/adr/](docs/adr/).

| Decision                  | Choice                  |
|---------------------------|-------------------------|
| Architecture              | Hexagonal               |
| Concurrency               | Atomic SQL UPDATE       |
| Geo service resilience    | Circuit breaker         |
| Domain primitives         | Value objects           |
| Error mapping             | @RestControllerAdvice   |
| Adapter testing           | WireMock                |
