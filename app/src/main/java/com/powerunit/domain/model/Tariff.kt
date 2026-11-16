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

data class SlabInfo(
    val slabNumber: Int,
    val ratePerUnit: BigDecimal,
    val fromUnit: BigDecimal,
    val toUnit: BigDecimal? = null
)

val DEFAULT_BANGLADESH_SLABS = listOf(
    TariffSlab(id = 1, tariffId = 1, fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.85")),
    TariffSlab(id = 2, tariffId = 1, fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
    TariffSlab(id = 3, tariffId = 1, fromUnit = BigDecimal("201"), toUnit = BigDecimal("300"), ratePerUnit = BigDecimal("6.95")),
    TariffSlab(id = 4, tariffId = 1, fromUnit = BigDecimal("301"), toUnit = BigDecimal("400"), ratePerUnit = BigDecimal("7.34")),
    TariffSlab(id = 5, tariffId = 1, fromUnit = BigDecimal("401"), toUnit = BigDecimal("600"), ratePerUnit = BigDecimal("11.51")),
    TariffSlab(id = 6, tariffId = 1, fromUnit = BigDecimal("601"), toUnit = null, ratePerUnit = BigDecimal("13.26"))
)

val DEFAULT_BANGLADESH_TARIFF = Tariff(
    id = 1,
    name = "Default Tariff (LT-A)",
    effectiveFrom = kotlinx.datetime.Instant.fromEpochMilliseconds(0),
    vatPercent = BigDecimal("5.0"),
    serviceChargeFlat = BigDecimal("10.00"),
    demandChargeFlat = BigDecimal("35.00"),
    isActive = true,
    slabs = DEFAULT_BANGLADESH_SLABS,
    rebatePercent = BigDecimal.ZERO,
    otherCharges = BigDecimal.ZERO,
    effectiveFromCycle = "1970-01"
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
