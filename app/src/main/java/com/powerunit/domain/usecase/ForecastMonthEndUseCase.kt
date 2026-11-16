package com.powerunit.domain.usecase

import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.MonthEndForecast
import com.powerunit.domain.model.Tariff
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.sqrt

class ForecastMonthEndUseCase @Inject constructor(
    private val calculateBillUseCase: CalculateBillUseCase
) {

    operator fun invoke(
        dailyUsages: List<DailyUsage>,
        cycleUsage: CycleUsage,
        tariff: Tariff
    ): MonthEndForecast {
        val daysLeft = cycleUsage.daysLeft

        if (daysLeft <= 0) {
            val currentBill = calculateBillUseCase(cycleUsage.totalUnitsSoFar, tariff).totalBill
            val currentSlab = calculateBillUseCase.getSlabForUnits(cycleUsage.totalUnitsSoFar, tariff)
            val unitsToNext = calculateBillUseCase.getUnitsToNextSlab(cycleUsage.totalUnitsSoFar, tariff)
            return MonthEndForecast(
                expectedUnits = cycleUsage.totalUnitsSoFar,
                lowUnits = cycleUsage.totalUnitsSoFar,
                highUnits = cycleUsage.totalUnitsSoFar,
                expectedBill = currentBill,
                lowBill = currentBill,
                highBill = currentBill,
                daysLeft = 0,
                projectedSlab = currentSlab,
                unitsToNextSlab = unitsToNext
            )
        }

        // Filter daily usages within current billing cycle
        val cycleUsages = dailyUsages
            .filter { it.date in cycleUsage.cycleStartDate..cycleUsage.cycleEndDate }

        // Prefer empirical non-estimated usages to avoid variance compression from linear interpolation
        val empiricalUsages = cycleUsages.filter { !it.isEstimated }
        val usagesForStats = if (empiricalUsages.isNotEmpty()) empiricalUsages else cycleUsages

        val sortedRecent = usagesForStats.sortedByDescending { it.date }
        val last7Days = sortedRecent.take(7)

        val avg7Days = if (last7Days.isNotEmpty()) {
            val sum7 = last7Days.fold(BigDecimal.ZERO) { acc, u -> acc + u.units }
            sum7.divide(BigDecimal(last7Days.size), 4, RoundingMode.HALF_UP)
        } else {
            cycleUsage.dailyAverage
        }

        val cycleAvg = cycleUsage.dailyAverage

        // Adaptive blend: scales gracefully from cold start to mature cycle
        // Early in cycle, weight is shared evenly; later in cycle, cycle average anchors the projection
        val daysElapsed = cycleUsage.daysElapsed.coerceAtLeast(1)
        val recentWeight = BigDecimal("7.0")
            .divide(BigDecimal(daysElapsed.coerceAtMost(21) + 7), 4, RoundingMode.HALF_UP)
        val cycleWeight = (BigDecimal.ONE - recentWeight).coerceAtLeast(BigDecimal.ZERO)

        val weightedRate = avg7Days.multiply(recentWeight)
            .add(cycleAvg.multiply(cycleWeight))

        val daysLeftBd = BigDecimal(daysLeft)
        val expectedAdditional = weightedRate.multiply(daysLeftBd)
        val expectedUnits = (cycleUsage.totalUnitsSoFar + expectedAdditional).setScale(1, RoundingMode.HALF_UP)

        // Derivation of confidence band from actual residual daily variance
        val margin = computeVarianceConfidenceMargin(usagesForStats, expectedAdditional, daysLeft)

        val lowUnits = (cycleUsage.totalUnitsSoFar + (expectedAdditional - margin).coerceAtLeast(BigDecimal.ZERO))
            .setScale(1, RoundingMode.HALF_UP)
        val highUnits = (cycleUsage.totalUnitsSoFar + expectedAdditional + margin)
            .setScale(1, RoundingMode.HALF_UP)

        val expectedBill = calculateBillUseCase(expectedUnits, tariff).totalBill
        val lowBill = calculateBillUseCase(lowUnits, tariff).totalBill
        val highBill = calculateBillUseCase(highUnits, tariff).totalBill

        val projectedSlab = calculateBillUseCase.getSlabForUnits(expectedUnits, tariff)
        val unitsToNextSlab = calculateBillUseCase.getUnitsToNextSlab(expectedUnits, tariff)

        return MonthEndForecast(
            expectedUnits = expectedUnits,
            lowUnits = lowUnits,
            highUnits = highUnits,
            expectedBill = expectedBill,
            lowBill = lowBill,
            highBill = highBill,
            daysLeft = daysLeft,
            projectedSlab = projectedSlab,
            unitsToNextSlab = unitsToNextSlab
        )
    }

    /**
     * Derives a statistically grounded forecast uncertainty margin based on standard error of daily usage:
     * SE_total = sigma_daily * sqrt(daysLeft). Uses 90% confidence (z = 1.645) bounded within [5%, 35%].
     */
    private fun computeVarianceConfidenceMargin(
        usages: List<DailyUsage>,
        expectedAdditional: BigDecimal,
        daysLeft: Int
    ): BigDecimal {
        if (usages.size < 3 || expectedAdditional <= BigDecimal.ZERO) {
            // Cold start default margin: +/-15% of expected remaining units
            return expectedAdditional.multiply(BigDecimal("0.15")).setScale(1, RoundingMode.HALF_UP)
        }

        val doubleValues = usages.map { it.units.toDouble() }
        val mean = doubleValues.average()
        val variance = doubleValues.sumOf { (it - mean) * (it - mean) } / (doubleValues.size - 1)
        val stdDev = sqrt(variance.coerceAtLeast(0.0))

        val standardError = stdDev * sqrt(daysLeft.toDouble())
        val zScore90 = 1.645
        val rawMargin = BigDecimal.valueOf(zScore90 * standardError).setScale(1, RoundingMode.HALF_UP)

        // Rational clamping: lower bound 5% of remaining, upper bound 35% of remaining
        val minMargin = expectedAdditional.multiply(BigDecimal("0.05"))
        val maxMargin = expectedAdditional.multiply(BigDecimal("0.35"))
        return rawMargin.coerceIn(minMargin, maxMargin)
    }
}
