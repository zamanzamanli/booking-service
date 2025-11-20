package com.escape.booking.controller

import com.escape.booking.model.BulkCreateTimeSlotsRequest
import com.escape.booking.model.BulkCreateTimeSlotsResponse
import com.escape.booking.model.CreateTimeSlotRequest
import com.escape.booking.model.ListTimeSlotsResponse
import com.escape.booking.model.TimeSlot
import com.escape.booking.service.TimeSlotService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms/{roomId}/time-slots")
class RoomTimeSlotController(
    private val timeSlotService: TimeSlotService,
) {

    @GetMapping
    fun listTimeSlots(@PathVariable("roomId") roomId: Long): ListTimeSlotsResponse {
        val timeSlots = timeSlotService.listTimeSlots(roomId)
        return ListTimeSlotsResponse(timeSlots = timeSlots)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTimeSlot(
        @PathVariable roomId: Long,
        @RequestBody body: CreateTimeSlotRequest
    ): TimeSlot {
        return timeSlotService.createTimeSlot(roomId,body)
    }

    @PostMapping(":bulk")
    fun createBulkTimeSlots(
        @PathVariable roomId: Long,
        @RequestBody bulkCreateTimeSlotsRequest: BulkCreateTimeSlotsRequest
    ): BulkCreateTimeSlotsResponse {
        val timeSlots = timeSlotService.createTimeSlots(roomId,bulkCreateTimeSlotsRequest)
        return BulkCreateTimeSlotsResponse(timeSlots)
    }
}
