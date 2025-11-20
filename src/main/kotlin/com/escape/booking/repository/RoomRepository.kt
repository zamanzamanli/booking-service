package com.escape.booking.repository

import com.escape.booking.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository :
    JpaRepository<RoomEntity,Long>,
    JpaSpecificationExecutor<RoomEntity>
