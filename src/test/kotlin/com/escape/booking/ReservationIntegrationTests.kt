package com.escape.booking

import com.escape.booking.entity.ReservationEntity
import com.escape.booking.entity.RoomEntity
import com.escape.booking.entity.TimeSlotEntity
import com.escape.booking.model.CreateReservationRequest
import com.escape.booking.model.ReservationStatus
import com.escape.booking.repository.ReservationRepository
import com.escape.booking.repository.RoomRepository
import com.escape.booking.repository.TimeSlotRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ReservationIntegrationTests {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("booking_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var timeSlotRepository: TimeSlotRepository

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    private lateinit var testRoom: RoomEntity
    private lateinit var testTimeSlot: TimeSlotEntity

    @BeforeEach
    fun setUp() {
        // Create a test room
        testRoom = roomRepository.save(
            RoomEntity(
                name = "Test Room",
                description = "Test room for integration tests"
            )
        )

        // Create a test time slot
        testTimeSlot = timeSlotRepository.save(
            TimeSlotEntity(
                roomId = testRoom.id,
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(1)
            )
        )
    }

    @AfterEach
    fun tearDown() {
        reservationRepository.deleteAll()
        timeSlotRepository.deleteAll()
        roomRepository.deleteAll()
    }

    /**
     * Helper method to expire holds within a transaction context
     * This is needed because @Modifying queries require a transaction
     */
    fun expireHoldsInTransaction(seconds: Long): Int {
        return transactionTemplate.execute { 
            reservationRepository.releaseExpiredHolds(seconds)
        } ?: 0
    }

    /**
     * Test 1: Verify that reservations expire after the hold time
     * This test simulates the expiration logic by directly calling the repository method
     * that the scheduled job uses.
     */
    @Test
    fun `should expire reservations after hold time`() {
        val userId = UUID.randomUUID()
        val request = CreateReservationRequest(timeSlotId = testTimeSlot.id)

        // Create a reservation via the API
        val result = mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))
            .andReturn()

        val reservationId = objectMapper.readTree(result.response.contentAsString)
            .get("id").asLong()

        // Verify the reservation is in HOLD status
        var reservation = reservationRepository.findById(reservationId).get()
        assertEquals(ReservationStatus.HOLD, reservation.status)

        // Simulate expiration by calling the repository method with 0 seconds
        // (meaning all HOLD reservations should expire)
        val expiredCount = expireHoldsInTransaction(0)

        // Verify that one reservation was expired
        assertEquals(1, expiredCount)

        // Verify the reservation status changed to EXPIRED
        reservation = reservationRepository.findById(reservationId).get()
        assertEquals(ReservationStatus.EXPIRED, reservation.status)
    }

    /**
     * Test 2: Test race condition when two different users try to reserve the same time slot simultaneously
     * Only one should succeed, the other should get a conflict error.
     */
    @Test
    fun `should handle race condition when two different users reserve same time slot`() {
        val user1 = UUID.randomUUID()
        val user2 = UUID.randomUUID()
        val request = CreateReservationRequest(timeSlotId = testTimeSlot.id)

        // Use a CountDownLatch to ensure both threads start at approximately the same time
        val latch = CountDownLatch(1)
        val executor: ExecutorService = Executors.newFixedThreadPool(2)

        // Submit both reservation attempts concurrently
        val future1 = CompletableFuture.supplyAsync({
            latch.await() // Wait for the signal to start
            try {
                mockMvc.perform(
                    post("/api/reservations")
                        .header("X-User-Id", user1.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andReturn()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }, executor)

        val future2 = CompletableFuture.supplyAsync({
            latch.await() // Wait for the signal to start
            try {
                mockMvc.perform(
                    post("/api/reservations")
                        .header("X-User-Id", user2.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                ).andReturn()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }, executor)

        // Release both threads to start simultaneously
        latch.countDown()

        // Wait for both to complete
        val result1 = future1.get()
        val result2 = future2.get()

        executor.shutdown()

        // One should succeed (200 or 201), one should fail with conflict (409 or similar)
        val status1 = result1.response.status
        val status2 = result2.response.status

        // Exactly one should succeed
        val successCount = listOf(status1, status2).count { it == 200 || it == 201 }
        val conflictCount = listOf(status1, status2).count { it == 409 || it == 400 }

        assertTrue(successCount == 1, "Exactly one reservation should succeed")
        assertTrue(conflictCount == 1, "Exactly one reservation should fail with conflict")

        // Verify only one reservation exists in the database for this time slot
        val reservations = reservationRepository.findAll().filter { 
            it.timeSlotId == testTimeSlot.id && it.status == ReservationStatus.HOLD 
        }
        assertEquals(1, reservations.size, "Only one HOLD reservation should exist")
    }

    /**
     * Test 3: Test idempotency - same user reserving the same time slot twice
     * Should return the existing reservation instead of creating a new one.
     */
    @Test
    fun `should return existing reservation when same user reserves same time slot twice`() {
        val userId = UUID.randomUUID()
        val request = CreateReservationRequest(timeSlotId = testTimeSlot.id)

        // First reservation
        val result1 = mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))
            .andReturn()

        val reservation1Id = objectMapper.readTree(result1.response.contentAsString)
            .get("id").asLong()

        // Second reservation attempt by the same user
        val result2 = mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))
            .andReturn()

        val reservation2Id = objectMapper.readTree(result2.response.contentAsString)
            .get("id").asLong()

        // Both should return the same reservation ID
        assertEquals(reservation1Id, reservation2Id, "Same reservation should be returned for idempotent requests")

        // Verify only one reservation exists in the database for this user and time slot
        val reservations = reservationRepository.findAll().filter {
            it.userId == userId && it.timeSlotId == testTimeSlot.id
        }
        assertEquals(1, reservations.size, "Only one reservation should exist")
    }

    /**
     * Test 4: Test race condition between user confirming and system expiring a reservation
     * This simulates the scenario where a user confirms a reservation at almost the same time
     * the scheduled job tries to expire it. Only one operation should succeed.
     */
    @Test
    fun `should handle race condition between confirm and expiration`() {
        val userId = UUID.randomUUID()
        val request = CreateReservationRequest(timeSlotId = testTimeSlot.id)

        // Create a reservation
        val createResult = mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))
            .andReturn()

        val reservationId = objectMapper.readTree(createResult.response.contentAsString)
            .get("id").asLong()

        // Use a CountDownLatch to ensure both operations start at approximately the same time
        val latch = CountDownLatch(1)
        val executor: ExecutorService = Executors.newFixedThreadPool(2)

        // Track results
        var confirmStatus: Int? = null
        var confirmException: Exception? = null
        var expireCount: Int? = null

        // Submit confirm operation
        val confirmFuture = CompletableFuture.runAsync({
            latch.await() // Wait for the signal to start
            try {
                val result = mockMvc.perform(
                    post("/api/reservations/$reservationId/confirm")
                        .header("X-User-Id", userId.toString())
                ).andReturn()
                confirmStatus = result.response.status
            } catch (e: Exception) {
                confirmException = e
            }
        }, executor)

        // Submit expiration operation
        val expireFuture = CompletableFuture.runAsync({
            latch.await() // Wait for the signal to start
            try {
                expireCount = expireHoldsInTransaction(0)
            } catch (e: Exception) {
                // Shouldn't happen, but capture if it does
                throw RuntimeException("Expiration failed", e)
            }
        }, executor)

        // Release both threads to start simultaneously
        latch.countDown()

        // Wait for both to complete
        confirmFuture.get()
        expireFuture.get()

        executor.shutdown()

        // Verify the final state
        val finalReservation = reservationRepository.findById(reservationId).get()

        // The reservation should be in either CONFIRMED or EXPIRED state, not HOLD
        assertTrue(
            finalReservation.status == ReservationStatus.CONFIRMED || finalReservation.status == ReservationStatus.EXPIRED,
            "Reservation should be either CONFIRMED or EXPIRED, but was ${finalReservation.status}"
        )

        // Verify behavior based on which operation won the race
        if (finalReservation.status == ReservationStatus.CONFIRMED) {
            // Confirm won the race
            assertEquals(200, confirmStatus, "Confirm should succeed with 200 OK if it won the race")
            assertEquals(0, expireCount, "Expiration should update 0 rows if confirm won")
        } else {
            // Expiration won the race - reservation is EXPIRED
            assertEquals(ReservationStatus.EXPIRED, finalReservation.status)
            assertEquals(1, expireCount, "Expiration should update 1 row if it won the race")
            // Confirm should fail with 400 Bad Request because the status transition
            // from EXPIRED to CONFIRMED is not allowed (ReservationInvalidStatusTransitionRequestedException)
            assertTrue(
                confirmStatus == 400 || confirmException != null,
                "Confirm should fail with 400 if expiration won the race, got status: $confirmStatus"
            )
        }

        // Regardless of which won, the system should handle it gracefully without data corruption
        println("Race condition result: Reservation is ${finalReservation.status}, " +
                "confirmStatus=$confirmStatus, expireCount=$expireCount")
    }

    /**
     * Additional Test: Verify expired reservations don't prevent new reservations
     */
    @Test
    fun `should allow new reservation after previous reservation expired`() {
        val user1 = UUID.randomUUID()
        val user2 = UUID.randomUUID()
        val request = CreateReservationRequest(timeSlotId = testTimeSlot.id)

        // User 1 creates a reservation
        mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", user1.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))

        // Expire all HOLD reservations
        val expiredCount = expireHoldsInTransaction(0)
        assertEquals(1, expiredCount)

        // User 2 should now be able to create a new reservation
        mockMvc.perform(
            post("/api/reservations")
                .header("X-User-Id", user2.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("HOLD"))
            .andExpect(jsonPath("$.userId").value(user2.toString()))

        // Verify two reservations exist: one EXPIRED, one HOLD
        val allReservations = reservationRepository.findAll().filter { 
            it.timeSlotId == testTimeSlot.id 
        }
        assertEquals(2, allReservations.size)
        assertEquals(1, allReservations.count { it.status == ReservationStatus.EXPIRED })
        assertEquals(1, allReservations.count { it.status == ReservationStatus.HOLD })
    }
}

