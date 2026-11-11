package com.powerunit.domain.model

import kotlinx.datetime.LocalDate
import java.math.BigDecimal

data class DailyUsage(
    val date: LocalDate,
    val units: BigDecimal,
    val cost: BigDecimal,
    val isEstimated: Boolean = false,
    val isAnomaly: Boolean = false
)
