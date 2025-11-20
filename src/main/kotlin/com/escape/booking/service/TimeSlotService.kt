package com.escape.booking.service

import com.escape.booking.entity.TimeSlotEntity
import com.escape.booking.exception.OneOrMoreTimeSlotsOverlapException
import com.escape.booking.exception.TimeSlotNotFoundException
import com.escape.booking.exception.TimeSlotOverlapException
import com.escape.booking.model.BulkCreateTimeSlotsRequest
import com.escape.booking.model.CreateTimeSlotRequest
import com.escape.booking.model.TimeSlot
import com.escape.booking.repository.TimeSlotRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimeSlotService(
    private val roomService: RoomService,
    private val timeSlotRepository: TimeSlotRepository,
) {

    fun listTimeSlots(roomId: Long): List<TimeSlot> {
        validateRoomExists(roomId)
        val timeSlots = timeSlotRepository.findByRoomId(roomId)
        return timeSlots.map { it.toDto() }
    }

    fun getTimeSlot(id: Long): TimeSlot {
        val timeSlot = timeSlotRepository.findById(id).orElseThrow { TimeSlotNotFoundException(id) }
        return timeSlot.toDto()
    }

    fun createTimeSlot(roomId: Long,createTimeSlotRequest: CreateTimeSlotRequest): TimeSlot {
        validateRoomExists(roomId)
        val timeSlotEntity = TimeSlotEntity(
            roomId = roomId,
            startTime = createTimeSlotRequest.startTime,
            endTime = createTimeSlotRequest.endTime,
        )
        try {
            return timeSlotRepository.save(timeSlotEntity).toDto()
        } catch (_: DataIntegrityViolationException) {
            throw TimeSlotOverlapException(createTimeSlotRequest.startTime,createTimeSlotRequest.endTime)
        }
    }

    @Transactional
    fun createTimeSlots(roomId: Long,bulkCreateTimeSlotsRequest: BulkCreateTimeSlotsRequest): List<TimeSlot> {
        validateRoomExists(roomId)


        val timeSlotEntities = bulkCreateTimeSlotsRequest.createTimeSlotRequests.map {
            TimeSlotEntity(
                roomId = roomId,
                startTime = it.startTime,
                endTime = it.endTime
            )
        }
        try {
            return timeSlotRepository.saveAll(timeSlotEntities).map { it.toDto() }
        } catch (_: DataIntegrityViolationException) {
            throw OneOrMoreTimeSlotsOverlapException()
        }
    }

    private fun validateRoomExists(roomId: Long) {
        roomService.getRoom(roomId)
    }
}
