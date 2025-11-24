# Reservation Integration Tests

This document describes the integration tests for the reservation API, focusing on the `reserve` endpoint and testing critical scenarios around expiration and race conditions.

## Test Suite Overview

The `ReservationIntegrationTests` class contains comprehensive tests for the reservation system with a focus on:
- **Expiration logic**: Testing that reservations properly expire after the hold time
- **Race conditions**: Testing concurrent access scenarios
- **Idempotency**: Testing that duplicate requests are handled correctly

## Race Condition Scenarios Tested

### Scenario 1: Two Users, One Time Slot (Test 2)
```
User A ──┐
         ├──> Time Slot #1 (HOLD) ──> Winner: One gets HOLD, one gets CONFLICT
User B ──┘
```

### Scenario 2: Confirm vs. Expiration (Test 4)
```
Time: T0                    T1 (simultaneous)                 T2 (result)
─────────────────────────────────────────────────────────────────────────
Reservation: HOLD  ──┬──> User: CONFIRM                 ──> CONFIRMED ✓
                     │                                   OR
                     └──> System: EXPIRE                ──> EXPIRED ✓

Critical: Only ONE operation succeeds, preventing invalid state transitions
```

## Test Infrastructure

### Testcontainers Setup
The tests use **Testcontainers** to spin up a real PostgreSQL database for integration testing. This ensures tests run against actual database constraints and behavior:
- PostgreSQL 16 Alpine container
- Automatic lifecycle management (starts before tests, stops after)
- Isolated test database for each test run

### Test Profile
Tests run with the `test` profile which:
- Disables scheduled tasks to prevent interference with manual expiration testing
- Uses Flyway migrations to set up the database schema
- Configures appropriate JPA/Hibernate settings for testing

### Transaction Management
The tests include a helper method `expireHoldsInTransaction(seconds: Long)` that wraps the `releaseExpiredHolds` repository call using Spring's `TransactionTemplate`. This is necessary because:
- The repository method uses `@Modifying` annotation for native queries
- `@Modifying` queries require an active transaction to execute
- `TransactionTemplate` provides programmatic transaction management without needing proxy beans
- Without this wrapper, tests would fail with `TransactionRequiredException`

Example usage:
```kotlin
fun expireHoldsInTransaction(seconds: Long): Int {
    return transactionTemplate.execute { 
        reservationRepository.releaseExpiredHolds(seconds)
    } ?: 0
}
```

## Tests Included

### 1. Expiration Test
**Test Name:** `should expire reservations after hold time`

**Purpose:** Verifies that the expiration mechanism works correctly by simulating what the scheduled job does.

**How it works:**
1. Creates a reservation via the API (status: HOLD)
2. Verifies the reservation is in HOLD status
3. Manually triggers the expiration logic by calling `releaseExpiredHolds(0)` (0 seconds means all HOLD reservations should expire)
4. Verifies the reservation status changed to EXPIRED

**Key Points:**
- Tests the core expiration logic used by the scheduled job
- Validates that the database query correctly identifies and updates expired reservations
- The actual scheduled job runs every 30 seconds and expires reservations older than 5 minutes (see `ReservationScheduledJobs`)

### 2. Race Condition Test
**Test Name:** `should handle race condition when two different users reserve same time slot`

**Purpose:** Tests that the system correctly handles concurrent reservation attempts for the same time slot.

**How it works:**
1. Creates two different user IDs
2. Uses `CompletableFuture` and `CountDownLatch` to ensure both requests start simultaneously
3. Both users attempt to reserve the same time slot at the same time
4. Verifies that exactly one succeeds and one fails with a conflict error
5. Confirms only one reservation exists in the database

**Key Points:**
- Tests database-level uniqueness constraints
- Validates proper exception handling for concurrent conflicts
- Uses `DataIntegrityViolationException` catching in the service layer
- The database constraint ensures only one active reservation per time slot

### 3. Idempotency Test
**Test Name:** `should return existing reservation when same user reserves same time slot twice`

**Purpose:** Verifies that duplicate reservation requests from the same user return the existing reservation instead of failing.

**How it works:**
1. User creates a reservation for a time slot
2. Same user attempts to reserve the same time slot again
3. Verifies both requests return the same reservation ID
4. Confirms only one reservation exists in the database

**Key Points:**
- Tests the idempotency logic in `ReservationService.reserve()`
- Important for retry scenarios and network issues
- Checks for existing HOLD or CONFIRMED reservations before creating new ones

### 4. Race Condition: Confirm vs. Expiration Test
**Test Name:** `should handle race condition between confirm and expiration`

**Purpose:** Tests the critical race condition where a user attempts to confirm a reservation at the same time the scheduled job tries to expire it.

**How it works:**
1. Creates a reservation (status: HOLD)
2. Uses `CompletableFuture` and `CountDownLatch` to trigger two operations simultaneously:
   - User confirms the reservation via `/api/reservations/{id}/confirm`
   - System expires the reservation via `releaseExpiredHolds(0)`
3. Verifies that one operation succeeds and the reservation ends up in either CONFIRMED or EXPIRED state
4. Validates the system handles this gracefully without data corruption

**Key Points:**
- **Real-world scenario**: This can happen when a reservation is about to expire and the user confirms at the last second
- Both operations use database UPDATE statements with WHERE conditions on status
- The operation that commits first wins (due to database transaction isolation)
- If confirm wins: reservation becomes CONFIRMED, expiration updates 0 rows
- If expiration wins: reservation becomes EXPIRED, confirm fails with invalid status transition error
- Tests the atomic nature of `updateStatusIfMatches` in the repository
- Demonstrates proper use of optimistic concurrency control

**Why This Test Matters:**
- Prevents double-state scenarios
- Validates database-level consistency
- Ensures user experience is predictable (either they successfully confirm or get a clear error)
- Tests the `ReservationInvalidStatusTransitionRequestedException` handling

### 5. Expired Reservation Cleanup Test
**Test Name:** `should allow new reservation after previous reservation expired`

**Purpose:** Verifies that expired reservations don't block new reservations for the same time slot.

**How it works:**
1. User 1 creates a reservation (status: HOLD)
2. Manually expire the reservation
3. User 2 creates a new reservation for the same time slot
4. Verifies User 2's reservation succeeds
5. Confirms two reservations exist: one EXPIRED, one HOLD

**Key Points:**
- Tests that the system correctly allows re-booking after expiration
- Validates that expired reservations are properly handled in conflict detection
- Ensures the database constraint only applies to active (HOLD/CONFIRMED) reservations

## Running the Tests

### Run all reservation tests:
```bash
mvn test -Dtest=ReservationIntegrationTests
```

### Run a specific test:
```bash
mvn test -Dtest=ReservationIntegrationTests#"should expire reservations after hold time"
```

### Prerequisites:
- Docker must be running (for Testcontainers)
- Maven must be installed
- Java 21 must be installed

## Database Schema Assumptions

The tests assume the following database setup (from Flyway migrations):
- `booking.reservations` table with a unique constraint on `(time_slot_id)` where `status IN ('HOLD', 'CONFIRMED')`
- Trigger for automatic `updated_at` timestamp updates
- Support for ENUM type for reservation status

## Key Reservation Logic

### Status Transitions:
- **HOLD** → **CONFIRMED**: User confirms reservation
- **HOLD** → **CANCELED**: User cancels reservation  
- **HOLD** → **EXPIRED**: System expires after 5 minutes
- **EXPIRED/CANCELED** → No transitions allowed (terminal states)

### Hold Time:
- Default: 5 minutes (300 seconds)
- Configurable via `ReservationScheduledJobs.RESERVATION_HOLD_TIME_IN_SECONDS`
- Scheduled job runs every 30 seconds to clean up expired holds

### Conflict Resolution:
- Database constraint prevents double-booking
- If constraint violation occurs, `ReservationConflictException` is thrown
- Idempotent behavior: same user + same time slot returns existing reservation

## Test Summary

The test suite includes **5 comprehensive integration tests**:
1. ✅ Expiration mechanism testing
2. ✅ Race condition: concurrent reservations by different users
3. ✅ Idempotency: duplicate requests from same user
4. ✅ Race condition: confirm vs. expiration (critical edge case)
5. ✅ Expired reservation cleanup and re-booking
