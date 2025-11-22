package com.escape.booking.exception

import com.escape.booking.exception.platform.BadRequestPlatformException
import com.escape.booking.model.ReservationStatus

class ReservationInvalidStatusTransitionRequestedException(reservationId: Long,from: ReservationStatus,to: ReservationStatus) :
    BadRequestPlatformException("Invalid status transition from $from to $to requested for reservation for $reservationId")
