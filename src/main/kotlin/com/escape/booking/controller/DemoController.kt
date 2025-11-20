package com.escape.booking.controller

import com.escape.booking.entity.Demo
import com.escape.booking.repository.DemoRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/demos")
class DemoController(private val demoRepository: DemoRepository) {

    @GetMapping
    fun getAllDemos(): List<Demo> {
        return demoRepository.findAll()
    }

    @PostMapping
    fun createDemo(@RequestBody demo: Demo): Demo {
        return demoRepository.save(demo)
    }

    @GetMapping("/{id}")
    fun getDemo(@PathVariable id: Long): Demo? {
        return demoRepository.findById(id).orElse(null)
    }
}

