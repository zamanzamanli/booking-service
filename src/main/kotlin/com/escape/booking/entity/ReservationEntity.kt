package com.escape.booking.entity

import com.escape.booking.model.Reservation
import com.escape.booking.model.ReservationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "reservations",schema = "booking")
data class ReservationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: UUID,
    val timeSlotId: Long,

    @Enumerated(EnumType.STRING)
    var status: ReservationStatus = ReservationStatus.HOLD,

    @Generated(event = [EventType.INSERT])
    @Column(updatable = false,insertable = false)
    val createdAt: Instant = Instant.now(),

    @Generated(event = [EventType.INSERT,EventType.UPDATE])
    @Column(insertable = false,updatable = false)
    val updatedAt: Instant = Instant.now(),
) {

    fun toDto(): Reservation {
        return Reservation(
            id = id,
            userId = userId,
            timeSlotId = timeSlotId,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
