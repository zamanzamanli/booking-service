package com.escape.booking.exception

import com.escape.booking.exception.platform.NotFoundPlatformException

/**
 * Exception thrown when a reservation is not found
 * Automatically maps to HTTP 404
 */
class ReservationNotFoundException(
    reservationId: Long
) : NotFoundPlatformException("Reservation with id $reservationId not found")
