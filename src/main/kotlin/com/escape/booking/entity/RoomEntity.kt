package com.escape.booking.entity

import com.escape.booking.model.Room
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.time.Instant

@Entity
@Table(name = "rooms",schema = "booking")
class RoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,
    var description: String? = null,

    @Generated(event = [EventType.INSERT])
    @Column(updatable = false,insertable = false)
    val createdAt: Instant = Instant.now(),

    @Generated(event = [EventType.INSERT,EventType.UPDATE])
    @Column(insertable = false,updatable = false)
    val updatedAt: Instant = Instant.now(),
) {

    fun toDto(): Room {
        return Room(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
