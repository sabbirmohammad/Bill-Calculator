package com.powerunit.domain.model

import java.math.BigDecimal

enum class PrepaidAlertLevel {
    NORMAL,
    LOW_BALANCE,
    CRITICAL
}

data class PrepaidStatus(
    val currentBalance: BigDecimal,
    val estimatedUnitsRemaining: BigDecimal,
    val dailyBurnCost: BigDecimal,
    val estimatedDaysRemaining: Int,
    val isRechargeSoonAlert: Boolean,
    val exactDaysRemaining: BigDecimal = BigDecimal.ZERO,
    val hoursRemaining: Int = 0,
    val alertLevel: PrepaidAlertLevel = PrepaidAlertLevel.NORMAL,
    val estimatedMonthEndDeduction: BigDecimal = BigDecimal.ZERO
)
