# ADR-006: WireMock for Geo-Location Adapter Testing

## Context
`IpApiGeoLocationAdapter` makes real HTTP calls to `ip-api.com`. We need tests that verify the adapter correctly handles successful responses, failure responses, HTTP errors, and circuit breaker behaviour. We need to decide at which level to test this.

## Decision
Use **WireMock** to stub the HTTP server in `IpApiGeoLocationAdapterTest`. Stub definitions live in JSON mapping files under `src/test/resources/wiremock/mappings/`, loaded automatically by `WireMockExtension` at test startup.

Each scenario uses a distinct IP address so all mapping files are loaded once and coexist without conflict:

| Mapping file                          | IP        | Scenario                         |
|---------------------------------------|-----------|----------------------------------|
| `geo-us-success.json`                 | 8.8.8.8   | Successful lookup → `US`         |
| `geo-pl-lowercase.json`               | 1.1.1.1   | Response with lowercase code     |
| `geo-fail-status.json`                | 9.9.9.9   | API returns `status: fail` → `GeoLocationServiceException` |
| `geo-server-error.json`               | 1.2.3.4   | HTTP 500 response                |
| `geo-server-error-circuit-breaker.json` | 5.5.5.5 | HTTP 500 to open circuit breaker |

## Alternatives Considered

| Approach | Why rejected |
|---|---|
| Mock `GeoLocationPort` (interface) | Already done in service and integration tests; tests nothing about the adapter's HTTP interaction |
| Mock `RestClient` directly | Brittle — tests Spring internal implementation details, breaks on Spring upgrades |
| Real `ip-api.com` in tests | Non-deterministic, requires network access, subject to rate limiting |

## Why JSON Mapping Files

Stub definitions in JSON files rather than programmatic `stubFor(...)` calls:
- Stub definitions are readable independently of the test code.
- Changes to the external API contract are visible as a diff on the mapping files.
- The test class contains no stub setup boilerplate — it reads as pure behaviour verification.

## Consequences

**Positive:**
- Tests the real HTTP interaction end-to-end: URL path, query parameters, JSON deserialization, error handling.
- Tests are deterministic and network-independent.
- Circuit breaker opening under HTTP failures is verifiable without mocking framework internals.

**Negative:**
- WireMock starts a real HTTP server on a random port — slightly slower than pure mock-based tests.
- `wiremock-standalone` is a large artifact (~10 MB) due to dependency shading, which avoids Jetty version conflicts with Spring Boot's embedded server.
