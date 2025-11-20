package com.escape.booking.entity

import com.escape.booking.model.TimeSlot
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "time_slots",schema = "booking")
data class TimeSlotEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val roomId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,

    @Generated(event = [EventType.INSERT])
    @Column(updatable = false,insertable = false)
    val createdAt: Instant = Instant.now(),

    @Generated(event = [EventType.INSERT,EventType.UPDATE])
    @Column(insertable = false,updatable = false)
    val updatedAt: Instant = Instant.now(),
) {

    fun toDto(): TimeSlot {
        return TimeSlot(
            id = id,
            roomId = roomId,
            startTime = startTime,
            endTime = endTime,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
