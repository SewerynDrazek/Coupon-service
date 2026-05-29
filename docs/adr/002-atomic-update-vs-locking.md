# ADR-002: Atomic SQL UPDATE for Concurrency Contract

## Context
Coupon volume must not be exceeded under concurrent requests ("first come, first served"). Multiple application instances may run simultaneously, so the solution must be correct across processes without shared in-memory state.

## Decision
Use a single atomic SQL UPDATE with a guard condition:

```sql
UPDATE coupons
SET spent = spent + 1
WHERE code = :code AND spent < volume
```

The number of affected rows is returned: `1` means success, `0` means the coupon is exhausted. No separate SELECT is needed.

## Per-User Limit

The per-user uniqueness constraint is enforced exclusively at the database level
via a UNIQUE constraint on `(coupon_code, user_id)`. The adapter catches
`DataIntegrityViolationException` from `saveAndFlush` and translates it to
`CouponAlreadyUsedException` — no prior SELECT is needed.

Order of operations in `useCoupon`:
1. `saveUsage` — INSERT with immediate flush; fails fast if user already used the coupon
2. `incrementSpentIfAvailable` — atomic UPDATE; if coupon is exhausted, usage record is deleted as compensation

## Alternatives Considered

| Approach                                     | Why rejected                                                                                 |
|----------------------------------------------|----------------------------------------------------------------------------------------------|
| Pessimistic locking (`SELECT FOR UPDATE`)    | Holds a row-level DB lock for the duration of the transaction; reduces throughput under load |
| Optimistic locking (`@Version`)              | Requires retry logic on `OptimisticLockException`; complicates the service layer             |
| JVM concurrency API (`synchronized`, `Lock`   | Not applicable in distributed systems                                                        |
## Consequences

**Positive:**
- Correct under any number of concurrent requests and application instances.
- No retry logic required — a `0` result is a definitive answer, not a conflict to retry.

**Negative:**
- The volume-guard logic lives in SQL, not in the domain model — a trade-off between correctness guarantees and domain purity.
