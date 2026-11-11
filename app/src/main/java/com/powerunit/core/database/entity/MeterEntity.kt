package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Entity(tableName = "meters")
data class MeterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "POSTPAID" or "PREPAID"
    val startReading: BigDecimal,
    val multiplierFactor: BigDecimal = BigDecimal.ONE,
    val isActive: Boolean = true,
    val createdAt: Instant
)
