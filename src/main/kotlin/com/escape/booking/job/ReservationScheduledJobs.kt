package com.escape.booking.job

import com.escape.booking.repository.ReservationRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReservationScheduledJobs(
    private val reservationRepository: ReservationRepository,
) {

    companion object {
        const val RESERVATION_HOLD_TIME_IN_SECONDS: Long = 5 * 60
    }

    @Scheduled(fixedDelay = 30_000,initialDelay = 10_000)
    @Transactional
    fun releaseExpiredHolds() {
        reservationRepository.releaseExpiredHolds(RESERVATION_HOLD_TIME_IN_SECONDS)
    }
}
