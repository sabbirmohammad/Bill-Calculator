package com.powerunit.domain.model

import kotlinx.datetime.LocalDate
import java.math.BigDecimal

data class CycleUsage(
    val cycleStartDate: LocalDate,
    val cycleEndDate: LocalDate,
    val totalUnitsSoFar: BigDecimal,
    val dailyAverage: BigDecimal,
    val daysElapsed: Int,
    val daysLeft: Int
)
