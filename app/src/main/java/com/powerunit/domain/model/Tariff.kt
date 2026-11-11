package com.powerunit.domain.model

import kotlinx.datetime.Instant
import java.math.BigDecimal

data class TariffSlab(
    val id: Long = 0,
    val tariffId: Long = 0,
    val fromUnit: BigDecimal,
    val toUnit: BigDecimal? = null,
    val ratePerUnit: BigDecimal
)

data class Tariff(
    val id: Long = 0,
    val name: String,
    val effectiveFrom: Instant,
    val vatPercent: BigDecimal,
    val serviceChargeFlat: BigDecimal,
    val demandChargeFlat: BigDecimal,
    val isActive: Boolean = true,
    val slabs: List<TariffSlab> = emptyList()
)
