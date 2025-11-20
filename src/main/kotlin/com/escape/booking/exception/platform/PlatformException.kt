package com.escape.booking.exception.platform

/**
 * Base exception for all platform exceptions
 */
abstract class PlatformException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
