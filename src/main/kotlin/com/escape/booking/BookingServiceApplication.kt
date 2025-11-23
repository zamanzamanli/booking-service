package com.escape.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BookingServiceApplication

fun main(args: Array<String>) {
    runApplication<BookingServiceApplication>(*args)
}
