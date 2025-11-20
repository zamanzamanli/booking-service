package com.escape.booking.exception.platform

/**
 * Exception that maps to HTTP 409 Conflict
 */
open class ConflictPlatformException(
    message: String,
    cause: Throwable? = null
) : PlatformException(message, cause)
