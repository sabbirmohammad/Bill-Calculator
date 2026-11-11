package com.powerunit.domain.model

import java.math.BigDecimal

data class MonthEndForecast(
    val expectedUnits: BigDecimal,
    val lowUnits: BigDecimal,
    val highUnits: BigDecimal,
    val expectedBill: BigDecimal,
    val lowBill: BigDecimal,
    val highBill: BigDecimal,
    val daysLeft: Int
)
