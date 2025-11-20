package com.escape.booking.model

import java.time.LocalDateTime

data class CreateTimeSlotRequest(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
