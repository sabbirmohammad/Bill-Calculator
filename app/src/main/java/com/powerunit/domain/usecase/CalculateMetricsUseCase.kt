package com.powerunit.domain.usecase

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class EnergyMetrics(
    val costPerDay: BigDecimal,
    val costPerUnitEffective: BigDecimal,
    val co2EstimateKg: BigDecimal,
    val treesEquivalentMonthly: BigDecimal = BigDecimal.ZERO
)

class CalculateMetricsUseCase @Inject constructor() {

    companion object {
        /**
         * Standard grid emission factor for Bangladesh (Department of Environment / BPDB Baseline):
         * ~0.55 kg CO2 per kWh based on natural gas and fuel-mix generation.
         */
        val DEFAULT_EMISSION_FACTOR_BD = BigDecimal("0.55")
        val TREE_MONTHLY_CO2_ABSORPTION_KG = BigDecimal("1.8142") // 21.77 kg/year / 12 months
    }

    operator fun invoke(
        totalUnits: BigDecimal,
        totalCost: BigDecimal,
        daysElapsed: Int,
        emissionFactorKgPerKwh: BigDecimal = DEFAULT_EMISSION_FACTOR_BD
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

        val co2 = nonNegativeUnits.multiply(emissionFactorKgPerKwh)
            .setScale(2, RoundingMode.HALF_UP)

        val trees = if (co2 > BigDecimal.ZERO) {
            co2.divide(TREE_MONTHLY_CO2_ABSORPTION_KG, 1, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
        }

        return EnergyMetrics(
            costPerDay = costPerDay,
            costPerUnitEffective = costPerUnit,
            co2EstimateKg = co2,
            treesEquivalentMonthly = trees
        )
    }

    operator fun invoke(
        totalUnits: BigDecimal,
        totalCost: BigDecimal,
        daysElapsed: Int,
        emissionFactorKgPerKwh: Double
    ): EnergyMetrics {
        return invoke(
            totalUnits = totalUnits,
            totalCost = totalCost,
            daysElapsed = daysElapsed,
            emissionFactorKgPerKwh = BigDecimal.valueOf(emissionFactorKgPerKwh)
        )
    }
}
