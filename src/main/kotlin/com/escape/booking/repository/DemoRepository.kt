package com.escape.booking.repository

import com.escape.booking.entity.Demo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DemoRepository : JpaRepository<Demo, Long>
