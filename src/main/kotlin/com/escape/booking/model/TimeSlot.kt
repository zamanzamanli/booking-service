package com.escape.booking.model

import java.time.Instant
import java.time.LocalDateTime

data class TimeSlot(
    val id: Long,
    val roomId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
