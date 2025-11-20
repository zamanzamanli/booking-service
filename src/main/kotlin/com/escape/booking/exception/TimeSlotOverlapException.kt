package com.escape.booking.exception

import com.escape.booking.exception.platform.ConflictPlatformException
import java.time.LocalDateTime

/**
 * Exception thrown when trying to create a timeslot that overlaps with another one
 * Automatically maps to HTTP 409
 */
class TimeSlotOverlapException(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
) : ConflictPlatformException("Time slot for $startTime and $endTime overlaps.")
