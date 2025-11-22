package com.escape.booking.service

import com.escape.booking.entity.ReservationEntity
import com.escape.booking.exception.ReservationConflictException
import com.escape.booking.exception.ReservationInvalidStatusTransitionRequestedException
import com.escape.booking.exception.ReservationNotFoundException
import com.escape.booking.exception.ReservationWrongTimeSlotException
import com.escape.booking.exception.TimeSlotNotFoundException
import com.escape.booking.model.CreateReservationRequest
import com.escape.booking.model.Reservation
import com.escape.booking.model.ReservationStatus
import com.escape.booking.repository.ReservationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReservationService(
    private val timeSlotService: TimeSlotService,
    private val reservationRepository: ReservationRepository
) {

    @Transactional
    fun reserve(userId: UUID,createReservationRequest: CreateReservationRequest): Pair<Reservation,Boolean> {
        val timeSlotId = createReservationRequest.timeSlotId

        // If the same reservation already exists, then return it
        val existingReservation = reservationRepository.findFirstByTimeSlotIdAndUserId(timeSlotId,userId)
        existingReservation?.let {
            if (it.status in setOf(ReservationStatus.HOLD,ReservationStatus.CANCELED)) {
                return it.toDto() to false
            }
        }

        verifyTimeSlotExists(timeSlotId)

        val reservationEntity = ReservationEntity(
            userId = userId,
            timeSlotId = timeSlotId
        )
        try {
            val saved = reservationRepository.save(reservationEntity)
            reservationRepository.flush()
            return saved.toDto() to true
        } catch (_: DataIntegrityViolationException) {
            throw ReservationConflictException(timeSlotId)
        }
    }

    @Transactional
    fun confirm(userId: UUID,reservationId: Long): Reservation {
        return changeStatusTo(userId,reservationId,ReservationStatus.CONFIRMED)
    }

    @Transactional
    fun cancel(userId: UUID,reservationId: Long): Reservation {
        return changeStatusTo(userId,reservationId,ReservationStatus.CANCELED)
    }

    private fun getReservationEntityOrThrowNotFound(userId: UUID,reservationId: Long): ReservationEntity {
        val existingReservation =
            reservationRepository.findFirstByUserIdAndId(userId,reservationId) ?: throw ReservationNotFoundException(reservationId)
        return existingReservation
    }

    private fun verifyTimeSlotExists(timeSlotId: Long) {
        try {
            timeSlotService.getTimeSlot(timeSlotId)
        } catch (_: TimeSlotNotFoundException) {
            throw ReservationWrongTimeSlotException(timeSlotId)
        }
    }

    /**
     * Changes status of the current HOLD reservation to the new status
     */
    private fun changeStatusTo(userId: UUID,reservationId: Long,newStatus: ReservationStatus): Reservation {
        val rowsAffected = reservationRepository.updateStatusIfMatches(
            userId = userId,
            id = reservationId,
            status = ReservationStatus.HOLD.name,
            newStatus = newStatus.name
        )

        if (rowsAffected == 1) {
            return reservationRepository.findById(reservationId).get().toDto()
        }

        // if no change happened
        val reservationEntity = getReservationEntityOrThrowNotFound(userId,reservationId)
        // If reservation is already confirmed, return
        if (reservationEntity.status == newStatus) {
            return reservationEntity.toDto()
        }

        throw ReservationInvalidStatusTransitionRequestedException(reservationId,reservationEntity.status,newStatus)
    }
}
