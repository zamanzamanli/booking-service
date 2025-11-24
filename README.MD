# Escape Room Booking API

A **Spring Boot** application using a **PostgreSQL** database to manage escape room bookings.

The API tracks **rooms**, **time slots**, and **reservations**.  
Users can temporarily **hold** a time slot for up to **5 minutes** before confirming or releasing it.

---

## 🐳 Running with Docker

You can run the entire stack (application + PostgreSQL) via:

```bash
docker compose --profile full up -d
```

For detailed Docker setup instructions and environment variable configuration, see [DOCKER.md](DOCKER.md).

---

## 🧩 System Overview

### Core Entities

- **Room** — represents a single escape room.
- **TimeSlot** — bookable time window (local wall time).
- **Reservation** — user's hold or booking for a slot.

### Hold Mechanism

- A reservation starts in `HOLD` status.
- The hold automatically expires after **5 minutes**.
- Expired holds transition to `EXPIRED` (system action).
- Users can explicitly confirm or cancel a hold at any time.

### Background Job

- A scheduled job runs **every 30 seconds**.
- It finds reservations where:
  ```sql
  status = 'HOLD'
  AND created_at <= now() - interval '5 minutes'
  ```
  and sets them to `EXPIRED`.
- Implemented in `ReservationScheduledJobs` using a single, safe SQL `UPDATE`.
- Uses conditional updates to ensure idempotency and safety.

### API Capabilities

- Create holds (`POST /reservations`)
- Confirm or cancel holds
- View available time slots
- Auto-expiration of stale holds handled by system job

---

## ⚙️ Concurrency and Data Integrity

| Mechanism | Purpose |
|-----------|---------|
| Partial unique index | Prevents multiple active reservations for the same time slot (`status IN ('HOLD','CONFIRMED')`). |
| Transactional conditional updates | Ensures only one actor (user or system sweeper) can change the row first. |
| FK constraint | Guarantees reservation always points to a valid time slot. |

### Example consistency guard

When changing reservation status:

```sql
UPDATE booking.reservations
   SET status = :newStatus
 WHERE id = :id
   AND user_id = :userId
   AND status = :expectedStatus;
```

This ensures:
- Only the first actor (user or system) succeeds.
- Second actor finds 0 affected rows → safe no-op.

---

## 🧪 Testing

Integration and race-condition tests are included.

**Key test coverage:**
- Parallel reservation attempts (race condition safety).
- Auto-expiration (scheduled job transitions `HOLD` → `EXPIRED`).
- Manual confirm/cancel transitions (`HOLD` → `CONFIRMED`/`CANCELED`).
- Expiration window validation.

See [src/test/kotlin/com/escape/booking/RESERVATION_TESTS.md](src/test/kotlin/com/escape/booking/RESERVATION_TESTS.md) for details.

To run tests inside Docker:

```bash
./test-docker.sh
```

---

## 🤖 AI / LLM Usage

AI tools were used for:
- Generating Spring Boot boilerplate (e.g., exception mapping, global error handler).
- Database migration and entity skeletons.
- Docker setup and helper scripts.
- Markdown documentation (README.md, DOCKER.md, test docs).
- Brainstorming architecture, data model, and concurrency handling strategies.

**Example inspiration:**  
The test synchronization logic using:
```kotlin
// Use a CountDownLatch to ensure both threads start at approximately the same time
```
was generated via AI-assisted brainstorming.

---

## ⚠️ Known Limitations

### 1. Expiration Window

Because the expiration job runs every **30 seconds**, a hold may persist for up to **5 minutes 30 seconds** before being marked `EXPIRED`.

This is a **known and accepted limitation**.

**From a product perspective:**
- Users are guaranteed a 5-minute hold.
- A slightly longer one (≤30 seconds) does not affect correctness or user experience.

**If stricter accuracy were required**, possible options include:
- Running the expiration job more frequently (e.g., every 10 seconds).
- Using per-hold delayed jobs for second-level precision (adds infrastructure complexity).
- Or relying on a **TTL-based logic**:  
  Treat any hold older than 5 minutes as “non-blocking” when listing available windows, and if a user attempts to book that slot, first expire the old hold and then create a new reservation.

However, this TTL-based approach introduces additional logic, more queries, and tighter concurrency handling. 
Hence, we intentionally chose the simpler approach—allowing a short window (≤30 seconds) where a hold may remain active beyond 5 minutes until the scheduled job expires it.

**Trade-off:**
- A time slot might appear unavailable for ≤30 seconds after its hold expires.
- Easier to reason about system state, simpler concurrency handling.

---

## 🧭 API Overview

High-level summary of main endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/rooms` | GET | List all rooms |
| `/api/rooms/{roomId}/time-slots` | GET | List available time slots for a room |
| `/api/reservations` | POST | Create a new hold (5-minute TTL) |
| `/api/reservations/{id}/confirm` | POST | Confirm a hold |
| `/api/reservations/{id}/cancel` | POST | Cancel (release) a hold |

For detailed API contracts, request/response examples, and schema information, see [API.md](API.md).

---

## 📚 Documentation
- **[API.md](API.md)** - Complete API documentation with endpoints, error codes, and examples
- **[DOCKER.md](DOCKER.md)** - Docker setup and configuration guide
- **[RESERVATION_TESTS.md](src/test/kotlin/com/escape/booking/RESERVATION_TESTS.md)** - Test documentation and coverage details

---

## 🚀 Quick Start

1. **Clone the repository**
   ```bash
   git clone git@github.com:zamanzamanli/booking-service.git
   cd booking-service
   ```

2. **Run with Docker**
   ```bash
   docker compose --profile full up -d
   ```

3. **Access the API**
   ```
   http://localhost:8080/api
   ```

4. **Run tests**
   ```bash
   ./test-docker.sh
   ```

For more detailed setup instructions, see [DOCKER.md](DOCKER.md).
