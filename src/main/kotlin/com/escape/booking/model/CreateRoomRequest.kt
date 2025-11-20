package com.escape.booking.model

data class CreateRoomRequest(
    val name: String,
    val description: String? = null,
)
