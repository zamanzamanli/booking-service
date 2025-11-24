# Quick Test Reference Guide

## Run All Reservation Tests
```bash
cd /Users/zzamanli/personal/code/booking-service
mvn test -Dtest=ReservationIntegrationTests
```

## Run Individual Tests

### Test 1: Expiration Mechanism
```bash
mvn test -Dtest=ReservationIntegrationTests#"should expire reservations after hold time"
```
**What it tests:** Verifies that reservations properly expire after the hold time.

### Test 2: Race Condition - Concurrent Reservations
```bash
mvn test -Dtest=ReservationIntegrationTests#"should handle race condition when two different users reserve same time slot"
```
**What it tests:** Two users trying to reserve the same slot simultaneously - only one should succeed.

### Test 3: Idempotency
```bash
mvn test -Dtest=ReservationIntegrationTests#"should return existing reservation when same user reserves same time slot twice"
```
**What it tests:** Same user making duplicate reservation requests - should return existing reservation.

### Test 4: Race Condition - Confirm vs. Expiration 🆕
```bash
mvn test -Dtest=ReservationIntegrationTests#"should handle race condition between confirm and expiration"
```
**What it tests:** User confirming at the same time system expires - only one should succeed.

### Test 5: Post-Expiration Cleanup
```bash
mvn test -Dtest=ReservationIntegrationTests#"should allow new reservation after previous reservation expired"
```
**What it tests:** Expired reservations don't block new reservations for the same time slot.

## Prerequisites

- ✅ Docker running (required for Testcontainers)
- ✅ Java 21 installed
- ✅ Maven installed

## Test Output

When tests run successfully, you'll see:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.escape.booking.ReservationIntegrationTests
...
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Troubleshooting

### Mockito warnings (can be ignored)
```
WARNING: A Java agent has been loaded dynamically
Mockito is currently self-attaching...
```
**Solution:** These warnings are informational only and don't affect test execution. They occur because Spring Boot's test framework uses Mockito. The tests will run successfully despite these warnings. If desired, you can suppress them by adding `-XX:+EnableDynamicAgentLoading` to your JVM options.

### Docker not running
```
Error: Could not find a valid Docker environment
```
**Solution:** Start Docker Desktop or Docker daemon.

### Port conflicts
```
Error: Bind for 0.0.0.0:XXXXX failed: port is already allocated
```
**Solution:** Testcontainers uses random ports. If you see this, another process is interfering. Stop other PostgreSQL instances or restart Docker.

### Database migration issues
```
Error: Flyway migration failed
```
**Solution:** Check that all migration files in `src/main/resources/db/migration/` are valid.

### Transaction-related errors
```
Error: No EntityManager with actual transaction available
```
**Solution:** This has been fixed by using the `expireHoldsInTransaction()` helper method with `TransactionTemplate`. This error occurs when calling `@Modifying` repository methods outside a transaction context. The helper method uses Spring's `TransactionTemplate` to properly wrap the call in a transaction.

## Test Execution Time

- Full test suite: ~15-30 seconds (includes Docker container startup)
- Individual test: ~5-10 seconds (container startup is cached after first test)

## Coverage

The tests cover:
- ✅ Expiration logic (scheduled job simulation)
- ✅ Concurrent access (race conditions)
- ✅ Database constraints (unique time slots)
- ✅ Transaction isolation (optimistic locking)
- ✅ API error handling
- ✅ Status transitions

