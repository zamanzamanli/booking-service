package com.escape.booking.exception

import com.escape.booking.exception.platform.ConflictPlatformException

/**
 * Exception thrown when trying to create a reservation that is gone
 * Automatically maps to HTTP 409
 */
class ReservationConflictException(
    timeSlotId: Long
) : ConflictPlatformException("Time slot $timeSlotId has already been reserved")
