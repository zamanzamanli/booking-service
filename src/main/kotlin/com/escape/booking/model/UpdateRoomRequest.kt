package com.escape.booking.model

data class UpdateRoomRequest(
    val name: String,
    val description: String? = null,
)
