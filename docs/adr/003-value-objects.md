# ADR-003: Value Objects for Domain Primitives

## Context
Several domain concepts — coupon code, volume, and user ID — carry specific validation and normalization rules:
- `code` must be non-blank and is case-insensitive (`SUMMER10` equals `summer10`)
- `volume` must be a positive number
- `userId` must be non-blank

Without explicit modelling, these rules are enforced only at the API boundary (bean validation) and can be bypassed internally.

## Decision
Model each concept as an immutable Java record with validation and normalization in its canonical constructor:

```java
public record Code(String value) {
    public Code(String value) {
        if (value == null || value.isEmpty()) throw new InvalidCouponCodeException();
        this.value = value.toUpperCase();
    }
}
```

## Consequences

**Positive:**
- It is impossible to construct an invalid or un-normalized `Code`, `Volume`, or `UserId` — any invalid input causes an immediate domain exception.
- Case normalization is co-located with the concept it belongs to, not scattered across service or controller layers.
- Type safety: the compiler prevents passing a `UserId` where a `Code` is expected.
- Tests that construct domain objects directly never need to worry about normalization — the VO handles it.

**Negative:**
- Small amount of wrapper code for each primitive.
- Slightly more conversion between raw strings (from HTTP/DB) and value objects.
