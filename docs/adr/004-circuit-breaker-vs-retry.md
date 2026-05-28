# ADR-004: Circuit Breaker over Retry for Geo-Location

## Context
The geo-location service (`ip-api.com`) is called during coupon usage to resolve the caller's country from their IP address. This call happens inside a `@Transactional` method, meaning a DB connection is held open for its duration. The service can be temporarily unavailable.

## Decision
Wrap the geo-location HTTP call in a **Resilience4j circuit breaker** rather than implementing retry with backoff.

`GeoLocationPort.getCountry()` always returns a country code string or throws — there is no empty/fallback result. Every failure path is surfaced as an exception and rejected with an appropriate HTTP error code.

## Why Not Retry

Retry with exponential backoff would hold the database connection open for the entire retry sequence. Under sustained geo-location failure this creates a connection pool exhaustion vector:

```
request → open TX → open DB connection → geo call fails → wait 1s → retry → wait 2s → retry
           ↑ connection held for 3+ seconds per request
```

With 50 concurrent requests and a pool of 10 connections, the pool exhausts quickly — a failure in an auxiliary service cascades into a full service outage.

## Why Circuit Breaker

The circuit breaker **fails fast** once the failure threshold is reached:

- While **CLOSED**: calls proceed normally; failures are counted.
- Once **OPEN**: `CallNotPermittedException` is thrown immediately — no HTTP call, no DB connection delay.
- After `waitDurationInOpenState`: transitions to **HALF-OPEN** to probe recovery.

This isolates the geo-location failure from the DB connection pool.

## Failure Handling

All failure modes are rejected explicitly — there is no permissive fallback that silently bypasses the country check:

| Failure mode | Thrown by | HTTP response |
|---|---|---|
| Private / local IP | `IpApiGeoLocationAdapter` | `403 Forbidden` |
| API returns `status: fail` | `IpApiGeoLocationAdapter` | `503 Service Unavailable` |
| HTTP error (`RestClientException`) | `IpApiGeoLocationAdapter` | `503 Service Unavailable` |
| Circuit breaker open | Re-thrown `CallNotPermittedException` | `503 Service Unavailable` |

This strict approach ensures the country restriction is never silently bypassed due to a geo-location failure. A caller whose country cannot be determined is rejected rather than permitted.

## Consequences

**Positive:**
- DB connections are not held open during geo-location failures.
- Sustained geo-location failure does not cascade into a service outage.
- Country restriction cannot be bypassed by exploiting a geo-location outage.
- Allows the geo-location service time to recover without a flood of retried requests.

**Negative:**
- Legitimate users may receive `503` during a geo-location outage rather than being served.
- Requires configuration tuning (window size, thresholds, wait duration).
