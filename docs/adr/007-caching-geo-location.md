# ADR-007: Cache Geo-Location Lookups

## Context

Every coupon usage triggers an HTTP call to `ip-api.com` to resolve the caller's country from their IP address. This creates two risks:

- **Rate limits** — ip-api.com enforces request quotas on the free tier. Under load, the service will start returning errors before any real infrastructure problem occurs.
- **Outages** — any transient unavailability of the third-party API causes `503` responses to callers, even when the country for that IP was already successfully resolved moments before.

## Decision

Cache the result of `GeoLocationPort.getCountry(ip)` using **Spring Cache with Caffeine** as the backing store.

The cache is applied at the port boundary — the `IpApiGeoLocationAdapter` is annotated with `@Cacheable("geoLocation")`. This keeps caching out of the domain and confined to the infrastructure layer, consistent with the hexagonal architecture (ADR-001).

Cache configuration:
- **Key**: the IP address string
- **TTL**: 24 hours
- **Maximum size**: bounded to prevent unbounded heap growth

## Consequences

**Positive:**
- Repeated calls from the same IP address cost zero external HTTP requests after the first.
- The service continues to function for cached IPs during a geo-location outage.
- Rate-limit pressure on ip-api.com is reduced proportionally to the cache hit rate.
- Average request latency decreases for cache hits.

**Negative:**
- A cached country code can become stale if an IP block is reassigned to a different country. Given typical reassignment timescales this is an acceptable trade-off for this use case.
- Adds a dependency on Caffeine and requires cache size/TTL tuning.
- The cache is in-process and not shared across instances; in a horizontally scaled deployment each instance builds its own cache independently, which increases the number of calls to ip-api.com compared to a shared cache. For the current single-instance deployment this is not a concern.
