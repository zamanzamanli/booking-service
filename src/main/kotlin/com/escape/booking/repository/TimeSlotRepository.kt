package com.escape.booking.repository

import com.escape.booking.entity.TimeSlotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TimeSlotRepository :
    JpaRepository<TimeSlotEntity,Long>,
    JpaSpecificationExecutor<TimeSlotEntity> {
    fun findByRoomId(roomId: Long): List<TimeSlotEntity>
    
    @Query(
        """
        SELECT ts.*
        FROM booking.time_slots ts
        LEFT JOIN booking.reservations r 
            ON ts.id = r.time_slot_id 
            AND r.status IN ('HOLD', 'CONFIRMED')
        WHERE ts.room_id = :roomId
            AND r.id IS NULL
        """,
        nativeQuery = true
    )
    fun findAvailableByRoomId(@Param("roomId") roomId: Long): List<TimeSlotEntity>
}
