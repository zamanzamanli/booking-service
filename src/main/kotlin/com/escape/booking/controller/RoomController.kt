package com.escape.booking.controller

import com.escape.booking.model.CreateRoomRequest
import com.escape.booking.model.ListRoomsResponse
import com.escape.booking.model.Room
import com.escape.booking.model.UpdateRoomRequest
import com.escape.booking.service.RoomService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService,
) {
    @GetMapping
    fun listRooms(): ListRoomsResponse {
        return ListRoomsResponse(roomService.listRooms())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(@RequestBody @Valid createRoomRequest: CreateRoomRequest): Room {
        return roomService.createRoom(createRoomRequest)
    }

    @PutMapping("/{id}")
    fun updateRoom(
        @PathVariable id: Long,
        @RequestBody @Valid updateRoomRequest: UpdateRoomRequest
    ): Room {
        return roomService.updateRoom(id,updateRoomRequest)
    }

    @GetMapping("/{id}")
    fun getRoom(@PathVariable id: Long): Room {
        return roomService.getRoom(id)
    }
}
