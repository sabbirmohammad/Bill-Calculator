package com.powerunit.domain.usecase

import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.MonthEndForecast
import com.powerunit.domain.model.Tariff
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

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
            return MonthEndForecast(
                expectedUnits = cycleUsage.totalUnitsSoFar,
                lowUnits = cycleUsage.totalUnitsSoFar,
                highUnits = cycleUsage.totalUnitsSoFar,
                expectedBill = currentBill,
                lowBill = currentBill,
                highBill = currentBill,
                daysLeft = 0
            )
        }

        // Recent 7 days average
        val cycleUsages = dailyUsages
            .filter { it.date in cycleUsage.cycleStartDate..cycleUsage.cycleEndDate }
            .sortedByDescending { it.date }

        val last7Days = cycleUsages.take(7)
        val avg7Days = if (last7Days.isNotEmpty()) {
            val sum7 = last7Days.fold(BigDecimal.ZERO) { acc, u -> acc + u.units }
            sum7.divide(BigDecimal(last7Days.size), 4, RoundingMode.HALF_UP)
        } else {
            cycleUsage.dailyAverage
        }

        val cycleAvg = cycleUsage.dailyAverage

        // Weighted daily rate: 7-day avg * 0.6 + cycle avg * 0.4
        val weightedRate = avg7Days.multiply(BigDecimal("0.6"))
            .add(cycleAvg.multiply(BigDecimal("0.4")))

        val daysLeftBd = BigDecimal(daysLeft)

        val expectedAdditional = weightedRate.multiply(daysLeftBd)
        val expectedUnits = (cycleUsage.totalUnitsSoFar + expectedAdditional).setScale(1, RoundingMode.HALF_UP)

        // Low bound: -15% on remaining usage
        val lowAdditional = expectedAdditional.multiply(BigDecimal("0.85"))
        val lowUnits = (cycleUsage.totalUnitsSoFar + lowAdditional).setScale(1, RoundingMode.HALF_UP)

        // High bound: +15% on remaining usage
        val highAdditional = expectedAdditional.multiply(BigDecimal("1.15"))
        val highUnits = (cycleUsage.totalUnitsSoFar + highAdditional).setScale(1, RoundingMode.HALF_UP)

        val expectedBill = calculateBillUseCase(expectedUnits, tariff).totalBill
        val lowBill = calculateBillUseCase(lowUnits, tariff).totalBill
        val highBill = calculateBillUseCase(highUnits, tariff).totalBill

        return MonthEndForecast(
            expectedUnits = expectedUnits,
            lowUnits = lowUnits,
            highUnits = highUnits,
            expectedBill = expectedBill,
            lowBill = lowBill,
            highBill = highBill,
            daysLeft = daysLeft
        )
    }
}
