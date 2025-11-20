package com.escape.booking.model

import java.time.LocalDateTime

/**
 * Standard error response structure
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val details: Map<String, Any>? = null
)

