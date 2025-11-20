package com.escape.booking.service

import com.escape.booking.entity.RoomEntity
import com.escape.booking.exception.RoomAlreadyExistsException
import com.escape.booking.exception.RoomNotFoundException
import com.escape.booking.model.CreateRoomRequest
import com.escape.booking.model.Room
import com.escape.booking.model.UpdateRoomRequest
import com.escape.booking.repository.RoomRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
) {

    fun listRooms(): List<Room> {
        return roomRepository.findAll().map { it.toDto() }
    }

    fun createRoom(createRoomRequest: CreateRoomRequest): Room {
        val newRoom = RoomEntity(
            name = createRoomRequest.name,
            description = createRoomRequest.description
        )
        try {
            val savedRoom = roomRepository.save(newRoom)
            return savedRoom.toDto()
        } catch (_: DataIntegrityViolationException) {
            throw RoomAlreadyExistsException(createRoomRequest.name)
        }
    }

    fun updateRoom(id: Long,updateRoomRequest: UpdateRoomRequest): Room {
        val roomEntity = roomRepository.findById(id).orElseThrow { RoomNotFoundException(id) }
        roomEntity.name = updateRoomRequest.name
        roomEntity.description = updateRoomRequest.description
        try {
            return roomRepository.save(roomEntity).toDto()
        } catch (_: DataIntegrityViolationException) {
            throw RoomAlreadyExistsException(updateRoomRequest.name)
        }
    }

    fun getRoom(id: Long): Room {
        val roomEntity = roomRepository.findById(id).orElseThrow { RoomNotFoundException(id) }
        return roomEntity.toDto()
    }
}
