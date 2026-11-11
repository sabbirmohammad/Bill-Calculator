package com.powerunit.domain.model

import kotlinx.datetime.Instant
import java.math.BigDecimal

data class Reading(
    val id: Long = 0,
    val meterId: Long,
    val readingValue: BigDecimal,
    val recordedAt: Instant,
    val note: String? = null,
    val photoUri: String? = null,
    val isEstimated: Boolean = false,
    val isMeterReset: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
