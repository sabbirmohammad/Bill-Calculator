package com.powerunit.domain.model

import java.math.BigDecimal

data class PeriodComparison(
    val currentUnits: BigDecimal,
    val previousUnits: BigDecimal,
    val percentChange: Double
)
