package com.escape.booking.controller

import com.escape.booking.model.CreateReservationRequest
import com.escape.booking.model.Reservation
import com.escape.booking.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    fun createReservation(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody @Valid createReservationRequest: CreateReservationRequest
    ): ResponseEntity<Reservation> {
        val result = reservationService.reserve(userId,createReservationRequest)
        if (result.second) {
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result.first)
        }
        return ResponseEntity.ok(result.first)
    }

    @PostMapping("/{id}/confirm")
    fun confirmReservation(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable("id") reservationId: Long
    ): Reservation {
        return reservationService.confirm(userId,reservationId)
    }

    @PostMapping("/{id}/cancel")
    fun cancelReservation(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable("id") reservationId: Long
    ): Reservation {
        return reservationService.cancel(userId,reservationId)
    }

    @GetMapping("/{id}")
    fun getReservation(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable("id") reservationId: Long
    ): Reservation {
        return reservationService.getReservation(userId,reservationId)
    }
}
