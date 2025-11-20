package com.escape.booking.exception

import com.escape.booking.exception.platform.ConflictPlatformException

/**
 * Exception thrown when trying to create a timeslot that overlaps with another one
 * Automatically maps to HTTP 409
 */
class OneOrMoreTimeSlotsOverlapException() : ConflictPlatformException("One or more time slot(s) overlap.")
