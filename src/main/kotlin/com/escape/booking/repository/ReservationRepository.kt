package com.escape.booking.repository

import com.escape.booking.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReservationRepository :
    JpaRepository<ReservationEntity,Long>,
    JpaSpecificationExecutor<ReservationEntity> {
    fun findFirstByTimeSlotIdAndUserId(timeSlotId: Long,userId: UUID): ReservationEntity?
    fun findFirstByUserIdAndId(userId: UUID,id: Long): ReservationEntity?

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query(
        """
        UPDATE booking.reservations
           SET status = :newStatus
         WHERE id = :id
           AND user_id = :userId
           AND status = :status
        """,
        nativeQuery = true
    )
    fun updateStatusIfMatches(
        @Param("userId") userId: UUID,
        @Param("id") id: Long,
        @Param("status") status: String,
        @Param("newStatus") newStatus: String
    ): Int
}
