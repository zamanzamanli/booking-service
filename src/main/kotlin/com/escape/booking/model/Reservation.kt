package com.escape.booking.model

import java.time.Instant
import java.util.UUID

data class Reservation(
    val id: Long,
    val userId: UUID,
    val timeSlotId: Long,
    val status: ReservationStatus,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
