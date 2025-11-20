package com.escape.booking.model

import java.time.Instant

data class Room(
    val id: Long,
    val name: String,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
