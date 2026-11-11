package com.powerunit.domain.model

import kotlinx.datetime.Instant
import java.math.BigDecimal

enum class MeterType {
    POSTPAID,
    PREPAID
}

data class Meter(
    val id: Long = 0,
    val name: String,
    val type: MeterType,
    val startReading: BigDecimal,
    val multiplierFactor: BigDecimal = BigDecimal.ONE,
    val isActive: Boolean = true,
    val createdAt: Instant
)
