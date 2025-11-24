# Booking Service API Documentation

## Table of Contents
- [Overview](#overview)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [Escape Rooms](#escape-rooms)
  - [Time Slots](#time-slots)
  - [Reservations](#reservations)
- [Error Codes](#error-codes)
- [Data Models](#data-models)

## Overview

The Booking Service API provides endpoints for managing escape rooms, time slots, and reservations. The API follows RESTful principles and returns JSON responses.

**Base URL**: `/api`

## Authentication

All reservation endpoints require a user identifier passed via the `X-User-Id` header:

```
X-User-Id: <UUID>
```

Example:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

---

## API Endpoints

### Escape Rooms

#### List All Escape Rooms
```
GET /api/rooms
```

**Response**: `200 OK`
```json
{
  "rooms": [
    {
      "id": 1,
      "name": "The Haunted Mansion",
      "description": "A spooky escape room with supernatural themes and challenging puzzles",
      "createdAt": "2025-11-24T10:00:00Z",
      "updatedAt": "2025-11-24T10:00:00Z"
    }
  ]
}
```

#### Get Escape Room by ID
```
GET /api/rooms/{id}
```

**Path Parameters**:
- `id` (Long) - Room ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "The Haunted Mansion",
  "description": "A spooky escape room with supernatural themes and challenging puzzles",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Errors**:
- `404 Not Found` - Room not found

#### Create Escape Room
```
POST /api/rooms
```

**Request Body**:
```json
{
  "name": "The Haunted Mansion",
  "description": "A spooky escape room with supernatural themes and challenging puzzles"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "name": "The Haunted Mansion",
  "description": "A spooky escape room with supernatural themes and challenging puzzles",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Errors**:
- `400 Bad Request` - Invalid request body
- `409 Conflict` - Escape room with this name already exists

#### Update Escape Room
```
PUT /api/rooms/{id}
```

**Path Parameters**:
- `id` (Long) - Room ID

**Request Body**:
```json
{
  "name": "The Haunted Mansion - Remastered",
  "description": "Updated with new puzzles and enhanced horror elements"
}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "The Haunted Mansion - Remastered",
  "description": "Updated with new puzzles and enhanced horror elements",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T12:00:00Z"
}
```

**Errors**:
- `400 Bad Request` - Invalid request body
- `404 Not Found` - Escape room not found
- `409 Conflict` - Another escape room with this name already exists

---

### Time Slots

#### Get Time Slot by ID
```
GET /api/time-slots/{id}
```

**Path Parameters**:
- `id` (Long) - Time slot ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "roomId": 1,
  "startTime": "2025-11-25T10:00:00",
  "endTime": "2025-11-25T11:00:00",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Errors**:
- `404 Not Found` - Time slot not found

#### List Time Slots for Escape Room
```
GET /api/rooms/{roomId}/time-slots?availableOnly={boolean}
```

**Path Parameters**:
- `roomId` (Long) - Room ID

**Query Parameters**:
- `availableOnly` (Boolean, optional, default: `false`) - Filter to show only available time slots

**Response**: `200 OK`
```json
{
  "timeSlots": [
    {
      "id": 1,
      "roomId": 1,
      "startTime": "2025-11-25T10:00:00",
      "endTime": "2025-11-25T11:00:00",
      "createdAt": "2025-11-24T10:00:00Z",
      "updatedAt": "2025-11-24T10:00:00Z"
    }
  ]
}
```

#### Create Time Slot
```
POST /api/rooms/{roomId}/time-slots
```

**Path Parameters**:
- `roomId` (Long) - Room ID

**Request Body**:
```json
{
  "startTime": "2025-11-25T10:00:00",
  "endTime": "2025-11-25T11:00:00"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "roomId": 1,
  "startTime": "2025-11-25T10:00:00",
  "endTime": "2025-11-25T11:00:00",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Errors**:
- `400 Bad Request` - Invalid request body or time range
- `404 Not Found` - Room not found
- `409 Conflict` - Time slot overlaps with existing slot

#### Create Multiple Time Slots (Bulk)
```
POST /api/rooms/{roomId}/time-slots/bulk
```

**Path Parameters**:
- `roomId` (Long) - Room ID

**Request Body**:
```json
{
  "createTimeSlotRequests": [
    {
      "startTime": "2025-11-25T10:00:00",
      "endTime": "2025-11-25T11:00:00"
    },
    {
      "startTime": "2025-11-25T14:00:00",
      "endTime": "2025-11-25T15:00:00"
    }
  ]
}
```

**Response**: `200 OK`
```json
{
  "timeSlots": [
    {
      "id": 1,
      "roomId": 1,
      "startTime": "2025-11-25T10:00:00",
      "endTime": "2025-11-25T11:00:00",
      "createdAt": "2025-11-24T10:00:00Z",
      "updatedAt": "2025-11-24T10:00:00Z"
    },
    {
      "id": 2,
      "roomId": 1,
      "startTime": "2025-11-25T14:00:00",
      "endTime": "2025-11-25T15:00:00",
      "createdAt": "2025-11-24T10:00:00Z",
      "updatedAt": "2025-11-24T10:00:00Z"
    }
  ]
}
```

**Errors**:
- `400 Bad Request` - Invalid request body
- `404 Not Found` - Room not found
- `409 Conflict` - One or more time slots overlap with existing slots

---

### Reservations

All reservation endpoints require the `X-User-Id` header.

**Security Note**: For security purposes, if a reservation exists but doesn't belong to the user specified in the `X-User-Id` header, the API will return `404 Not Found` instead of a permission error. This prevents information disclosure about the existence of reservations belonging to other users.

#### Create Reservation
```
POST /api/reservations
```

**Idempotent**: Yes. If a reservation already exists for the same user and time slot, the existing reservation is returned instead of creating a duplicate.

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Request Body**:
```json
{
  "timeSlotId": 1
}
```

**Response**: `201 Created` (for new reservation) or `200 OK` (if reservation already exists)
```json
{
  "id": 1,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "timeSlotId": 1,
  "status": "HOLD",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Reservation Statuses**:
- `HOLD` - Initial reservation status (temporary hold)
- `CONFIRMED` - Reservation confirmed by user
- `CANCELED` - Reservation canceled by user
- `EXPIRED` - Reservation automatically expired (not confirmed in time)

**Errors**:
- `400 Bad Request` - Invalid time slot ID or request body
- `404 Not Found` - Time slot not found
- `409 Conflict` - Time slot already reserved by a different user (note: if the same user tries to reserve the same slot again, the existing reservation is returned with `200 OK`)

#### Get Reservation
```
GET /api/reservations/{id}
```

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Path Parameters**:
- `id` (Long) - Reservation ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "timeSlotId": 1,
  "status": "HOLD",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:00:00Z"
}
```

**Errors**:
- `404 Not Found` - Reservation not found or doesn't belong to the user in `X-User-Id` header

#### Confirm Reservation
```
POST /api/reservations/{id}/confirm
```

**Idempotent**: Yes. Confirming an already confirmed reservation will succeed and return the reservation in its confirmed state.

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Path Parameters**:
- `id` (Long) - Reservation ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "timeSlotId": 1,
  "status": "CONFIRMED",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:05:00Z"
}
```

**Valid Status Transitions**:
- `HOLD` → `CONFIRMED`
- `CONFIRMED` → `CONFIRMED` (idempotent)

**Errors**:
- `400 Bad Request` - Invalid status transition (e.g., trying to confirm a canceled or expired reservation)
- `404 Not Found` - Reservation not found or doesn't belong to the user in `X-User-Id` header

#### Cancel Reservation
```
POST /api/reservations/{id}/cancel
```

**Idempotent**: Yes. Canceling an already canceled reservation will succeed and return the reservation in its canceled state.

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Path Parameters**:
- `id` (Long) - Reservation ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "timeSlotId": 1,
  "status": "CANCELED",
  "createdAt": "2025-11-24T10:00:00Z",
  "updatedAt": "2025-11-24T10:10:00Z"
}
```

**Valid Status Transitions**:
- `HOLD` → `CANCELED`
- `CONFIRMED` → `CANCELED`
- `CANCELED` → `CANCELED` (idempotent)

**Errors**:
- `400 Bad Request` - Invalid status transition (e.g., trying to cancel an expired reservation)
- `404 Not Found` - Reservation not found or doesn't belong to the user in `X-User-Id` header

---

## Data Models

### Room
```typescript
{
  id: Long,
  name: String,
  description: String | null,
  createdAt: Instant,  // ISO-8601: "2025-11-24T10:00:00Z"
  updatedAt: Instant   // ISO-8601: "2025-11-24T10:00:00Z"
}
```

### TimeSlot
```typescript
{
  id: Long,
  roomId: Long,
  startTime: LocalDateTime,  // ISO-8601: "2025-11-25T10:00:00"
  endTime: LocalDateTime,    // ISO-8601: "2025-11-25T11:00:00"
  createdAt: Instant,        // ISO-8601: "2025-11-24T10:00:00Z"
  updatedAt: Instant         // ISO-8601: "2025-11-24T10:00:00Z"
}
```

### Reservation
```typescript
{
  id: Long,
  userId: UUID,              // "550e8400-e29b-41d4-a716-446655440000"
  timeSlotId: Long,
  status: ReservationStatus, // "HOLD" | "CONFIRMED" | "CANCELED" | "EXPIRED"
  createdAt: Instant,        // ISO-8601: "2025-11-24T10:00:00Z"
  updatedAt: Instant         // ISO-8601: "2025-11-24T10:00:00Z"
}
```

### ReservationStatus (Enum)
- `HOLD` - Reservation is on hold (temporary)
- `CONFIRMED` - Reservation is confirmed
- `CANCELED` - Reservation is canceled
- `EXPIRED` - Reservation has expired

**Valid Status Transitions**:
- `HOLD` → `CONFIRMED`
- `HOLD` → `CANCELED`
- `CONFIRMED` → `CANCELED`
- `CONFIRMED` → `CONFIRMED` (idempotent)
- `CANCELED` → `CANCELED` (idempotent)
- `HOLD` → `EXPIRED` (automatic, via scheduled job)

### ErrorResponse
```typescript
{
  timestamp: LocalDateTime,  // ISO-8601: "2025-11-24T10:00:00"
  status: Integer,           // HTTP status code
  error: String,             // HTTP status reason phrase
  message: String,           // Error description
  path: String | null,       // Request path
  details: Object | null     // Additional error details (e.g., validation errors)
}
```

---

## Date/Time Formats

The API uses ISO-8601 format for dates and times:

- **LocalDateTime**: `yyyy-MM-ddTHH:mm:ss` (e.g., `"2025-11-25T10:00:00"`)
  - Used for: Time slot start/end times
  - No timezone information

- **Instant**: `yyyy-MM-ddTHH:mm:ssZ` (e.g., `"2025-11-24T10:00:00Z"`)
  - Used for: Created/updated timestamps
  - Always in UTC (Z suffix)

---

## Examples

### Example: Creating an Escape Room and Time Slots

1. Create an escape room:
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "The Haunted Mansion",
    "description": "A spooky escape room with supernatural themes and challenging puzzles"
  }'
```

2. Create time slots for the escape room:
```bash
curl -X POST http://localhost:8080/api/rooms/1/time-slots/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "createTimeSlotRequests": [
      {
        "startTime": "2025-11-25T10:00:00",
        "endTime": "2025-11-25T11:00:00"
      },
      {
        "startTime": "2025-11-25T14:00:00",
        "endTime": "2025-11-25T15:00:00"
      }
    ]
  }'
```

3. List available time slots:
```bash
curl -X GET "http://localhost:8080/api/rooms/1/time-slots?availableOnly=true"
```

### Example: Making a Reservation

1. Create a reservation (puts it on hold):
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "timeSlotId": 1
  }'
```

2. Confirm the reservation:
```bash
curl -X POST http://localhost:8080/api/reservations/1/confirm \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000"
```

3. Or cancel the reservation:
```bash
curl -X POST http://localhost:8080/api/reservations/1/cancel \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000"
```

---

## Notes

- All POST/PUT requests require `Content-Type: application/json` header
- Reservation endpoints require `X-User-Id` header with a valid UUID
- Time slots cannot overlap for the same escape room
- Reservations start in `HOLD` status and must be confirmed or will expire automatically
- A scheduled job runs every 10 minutes to expire HOLD reservations older than 10 minutes
- **Idempotency**: Create, confirm, and cancel reservation operations are idempotent. Repeating the same operation will not cause errors and will return the expected result

