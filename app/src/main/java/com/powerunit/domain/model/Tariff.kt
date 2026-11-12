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
    val slabs: List<TariffSlab> = emptyList(),
    val rebatePercent: BigDecimal = BigDecimal.ZERO,
    val otherCharges: BigDecimal = BigDecimal.ZERO,
    val effectiveFromCycle: String? = null
)

data class TariffVersion(
    val id: Long = 0,
    val planId: Long = 1L,
    val effectiveFromCycle: String, // "YYYY-MM", e.g. "2026-11"
    val slabs: List<TariffSlab>,
    val fixedCharge: BigDecimal,
    val demandCharge: BigDecimal,
    val vatPercent: BigDecimal,
    val rebatePercent: BigDecimal = BigDecimal.ZERO,
    val otherCharges: BigDecimal = BigDecimal.ZERO,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toTariff(): Tariff {
        val instant = com.powerunit.core.common.DateUtils.cycleStringToInstant(effectiveFromCycle)
        return Tariff(
            id = id,
            name = "Tariff ($effectiveFromCycle)",
            effectiveFrom = instant,
            vatPercent = vatPercent,
            serviceChargeFlat = fixedCharge,
            demandChargeFlat = demandCharge,
            slabs = slabs,
            isActive = true,
            rebatePercent = rebatePercent,
            otherCharges = otherCharges,
            effectiveFromCycle = effectiveFromCycle
        )
    }
}
