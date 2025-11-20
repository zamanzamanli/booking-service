package com.escape.booking.exception.platform

/**
 * Exception that maps to HTTP 400 Bad Request
 */
open class BadRequestPlatformException(
    message: String,
    cause: Throwable? = null
) : PlatformException(message, cause)
