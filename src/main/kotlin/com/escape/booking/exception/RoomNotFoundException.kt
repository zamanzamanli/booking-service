package com.escape.booking.exception

import com.escape.booking.exception.platform.NotFoundPlatformException

/**
 * Exception thrown when a room is not found
 * Automatically maps to HTTP 404
 */
class RoomNotFoundException(
    roomId: Long
) : NotFoundPlatformException("Room with id $roomId not found")
