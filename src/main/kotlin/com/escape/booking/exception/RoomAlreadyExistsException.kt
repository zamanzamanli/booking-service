package com.escape.booking.exception

import com.escape.booking.exception.platform.ConflictPlatformException

/**
 * Exception thrown when trying to create a room that already exists
 * Automatically maps to HTTP 409
 */
class RoomAlreadyExistsException(
    roomName: String
) : ConflictPlatformException("Room with name '$roomName' already exists")
