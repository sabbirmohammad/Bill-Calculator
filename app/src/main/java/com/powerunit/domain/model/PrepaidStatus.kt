package com.powerunit.domain.model

import java.math.BigDecimal

data class PrepaidStatus(
    val currentBalance: BigDecimal,
    val estimatedUnitsRemaining: BigDecimal,
    val dailyBurnCost: BigDecimal,
    val estimatedDaysRemaining: Int,
    val isRechargeSoonAlert: Boolean
)
