package com.escape.booking.controller

import com.escape.booking.model.TimeSlot
import com.escape.booking.service.TimeSlotService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/time-slots")
class TimeSlotController(
    private val timeSlotService: TimeSlotService
) {

    @GetMapping("/{id}")
    fun getTimeSlot(@PathVariable id: Long): TimeSlot =
        timeSlotService.getTimeSlot(id)
}
