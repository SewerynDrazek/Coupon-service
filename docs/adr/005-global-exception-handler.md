# ADR-005: Centralized Exception Handling with @RestControllerAdvice

## Context
The service throws several domain exceptions (`CouponNotFoundException`, `CouponExhaustedException`, `CouponAlreadyUsedException`, `CouponCountryMismatchException`, etc.) that each need to be translated into an appropriate HTTP status code and a consistent error response body.

## Decision
Use a single `@RestControllerAdvice` class (`GlobalExceptionHandler`) that maps each exception type to an HTTP status and an `ErrorResponse` body via `@ExceptionHandler` methods.

## Alternatives Considered

| Approach | Why rejected |
|---|---|
| try-catch in each controller method | Controllers become verbose; mapping logic is duplicated across endpoints |
| Throwing `ResponseStatusException` from the service | Bleeds HTTP concerns into the domain layer |
| Custom `HandlerExceptionResolver` | Lower-level, more complex to implement for the same result |

## Consequences

**Positive:**
- Controllers contain only routing and delegation — no error-handling boilerplate.
- A single place to audit, add, or change all exception-to-status mappings.
- `ErrorResponse` format is consistent across all endpoints (status, message, timestamp).
- Adding a new exception type requires only a new `@ExceptionHandler` method in one class.
- The domain layer has no dependency on the HTTP layer — exceptions are plain Java exceptions.

**Negative:**
- The mapping between an exception and its HTTP status lives in a different file from the exception class, which requires a small navigation step when adding new exceptions.
