package com.escape.booking.model

data class BulkCreateTimeSlotsRequest(
    val createTimeSlotRequests: List<CreateTimeSlotRequest>,
)
