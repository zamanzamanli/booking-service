package com.escape.booking.exception.platform

/**
 * Exception that maps to HTTP 404 Not Found
 */
open class NotFoundPlatformException(
    message: String,
    cause: Throwable? = null
) : PlatformException(message, cause)
