package com.escape.booking.exception

import com.escape.booking.exception.platform.BadRequestPlatformException

class ReservationWrongTimeSlotException(
    timeSlotId: Long
) : BadRequestPlatformException("Time Slot with id $timeSlotId not found")
