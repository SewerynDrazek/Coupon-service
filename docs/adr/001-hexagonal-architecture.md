# ADR-001: Hexagonal Architecture

## Context
The service integrates with two external systems: a PostgreSQL database and a third-party HTTP geo-location API. It also exposes an HTTP API of its own. We needed a structural approach that would keep business logic testable and decoupled from infrastructure concerns.

## Decision
Adopt hexagonal architecture (ports & adapters):

- **Domain layer** — contains the `Coupon` aggregate, value objects, domain exceptions, and port interfaces (`CouponRepository`, `CouponUsageRepository`, `GeoLocationPort`). No framework dependencies.
- **API layer** — inbound adapter: `CouponController`, DTOs, input validation. Depends on the domain, not on infrastructure.
- **Infrastructure layer** — outbound adapters: JPA repositories implementing domain ports, `IpApiGeoLocationAdapter` implementing `GeoLocationPort`.

## Consequences

**Positive:**
- Domain logic (`CouponService`) is unit-testable with plain Mockito — no Spring context, no database, no HTTP calls required.
- Outbound dependencies (database, geo-location) can be swapped or mocked without touching business logic.
- The geo-location port is a clean example: `CouponIntegrationTest` replaces the real HTTP adapter with a mock, while `IpApiGeoLocationAdapterTest` tests only the adapter in isolation with WireMock.

**Negative:**
- More boilerplate than a simple layered (controller → service → repository) architecture.
- For a service of this size, the overhead is noticeable — this trade-off is intentional to demonstrate production-style structuring.
