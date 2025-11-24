package com.escape.booking

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BookingServiceApplicationTests {

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

	@Test
	fun contextLoads() {
		// Verifies that the Spring application context loads successfully with Testcontainers
	}

}
