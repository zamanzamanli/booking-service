package com.escape.booking.repository

import com.escape.booking.entity.TimeSlotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TimeSlotRepository :
    JpaRepository<TimeSlotEntity,Long>,
    JpaSpecificationExecutor<TimeSlotEntity> {
    fun findByRoomId(roomId: Long): List<TimeSlotEntity>
}
