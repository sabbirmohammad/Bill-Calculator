package com.powerunit.domain.usecase

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class EnergyMetrics(
    val costPerDay: BigDecimal,
    val costPerUnitEffective: BigDecimal,
    val co2EstimateKg: BigDecimal
)

class CalculateMetricsUseCase @Inject constructor() {

    operator fun invoke(
        totalUnits: BigDecimal,
        totalCost: BigDecimal,
        daysElapsed: Int,
        emissionFactorKgPerKwh: Double = 0.55
    ): EnergyMetrics {
        val nonNegativeUnits = totalUnits.coerceAtLeast(BigDecimal.ZERO)
        val nonNegativeCost = totalCost.coerceAtLeast(BigDecimal.ZERO)

        val costPerDay = if (daysElapsed > 0) {
            nonNegativeCost.divide(BigDecimal(daysElapsed), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        val costPerUnit = if (nonNegativeUnits.compareTo(BigDecimal.ZERO) > 0) {
            nonNegativeCost.divide(nonNegativeUnits, 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        }

        val co2 = nonNegativeUnits.multiply(BigDecimal.valueOf(emissionFactorKgPerKwh))
            .setScale(2, RoundingMode.HALF_UP)

        return EnergyMetrics(
            costPerDay = costPerDay,
            costPerUnitEffective = costPerUnit,
            co2EstimateKg = co2
        )
    }
}
