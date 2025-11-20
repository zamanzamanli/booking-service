package com.escape.booking.exception

import com.escape.booking.exception.platform.NotFoundPlatformException

/**
 * Exception thrown when a time slot is not found
 * Automatically maps to HTTP 404
 */
class TimeSlotNotFoundException(
    timeSlotId: Long
) : NotFoundPlatformException("Time Slot with id $timeSlotId not found")
