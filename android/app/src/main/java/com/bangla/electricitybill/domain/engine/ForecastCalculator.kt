package com.bangla.electricitybill.domain.engine

import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.ForecastResult
import com.bangla.electricitybill.domain.model.Tariff
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

object ForecastCalculator {

    /**
     * Calculates projected month-end consumption and estimated bill based on daily run rate.
     */
    fun calculateForecast(
        currentMonthlyConsumption: BigDecimal,
        currentDate: LocalDate = DateUtils.today(),
        tariff: Tariff,
        monthlyBudget: BigDecimal? = null
    ): ForecastResult {
        val yearMonth = YearMonth.from(currentDate)
        val totalDaysInMonth = yearMonth.lengthOfMonth().toLong()
        val elapsedDays = currentDate.dayOfMonth.toLong()
        val remainingDays = (totalDaysInMonth - elapsedDays).coerceAtLeast(0L)

        val averageDailyConsumption = if (elapsedDays > 0L) {
            currentMonthlyConsumption.divide(
                BigDecimal(elapsedDays),
                BigDecimalMath.CALCULATION_SCALE,
                RoundingMode.HALF_UP
            )
        } else {
            BigDecimal.ZERO
        }

        val projectedAdditionalConsumption = averageDailyConsumption.multiply(BigDecimal(remainingDays))
        val projectedTotalConsumption = currentMonthlyConsumption.add(projectedAdditionalConsumption)
            .setScale(BigDecimalMath.READING_SCALE, RoundingMode.HALF_UP)

        val projectedBillItemized = ProgressiveTariffEngine.calculateMonthlyBill(
            projectedTotalConsumption,
            tariff
        )

        val budgetExceeded = if (monthlyBudget != null && monthlyBudget > BigDecimal.ZERO) {
            projectedBillItemized.totalBill > monthlyBudget
        } else {
            false
        }

        return ForecastResult(
            currentConsumption = currentMonthlyConsumption,
            elapsedDays = elapsedDays,
            remainingDays = remainingDays,
            averageDailyConsumption = averageDailyConsumption.setScale(BigDecimalMath.READING_SCALE, RoundingMode.HALF_UP),
            projectedAdditionalConsumption = projectedAdditionalConsumption.setScale(BigDecimalMath.READING_SCALE, RoundingMode.HALF_UP),
            projectedTotalConsumption = projectedTotalConsumption,
            projectedEnergyCost = projectedBillItemized.energyCost,
            projectedTotalBill = projectedBillItemized.totalBill,
            budgetAmount = monthlyBudget,
            budgetExceeded = budgetExceeded
        )
    }
}
