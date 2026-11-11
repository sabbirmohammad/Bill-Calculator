package com.powerunit.domain.usecase

import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.DailyUsage
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateCycleUsageUseCase @Inject constructor() {

    operator fun invoke(
        dailyUsages: List<DailyUsage>,
        cycleStartDay: Int,
        today: LocalDate
    ): CycleUsage {
        val (cycleStart, cycleEnd) = computeCycleRange(cycleStartDay, today)

        val cycleDailyUsages = dailyUsages.filter { it.date in cycleStart..today }
        val totalUnits = cycleDailyUsages.fold(BigDecimal.ZERO) { acc, usage -> acc + usage.units }
            .setScale(1, RoundingMode.HALF_UP)

        val totalCycleDays = (cycleEnd.toEpochDays() - cycleStart.toEpochDays() + 1).coerceAtLeast(1)
        val daysElapsed = (today.toEpochDays() - cycleStart.toEpochDays() + 1).coerceIn(1, totalCycleDays)
        val daysLeft = (totalCycleDays - daysElapsed).coerceAtLeast(0)

        val dailyAverage = if (daysElapsed > 0) {
            totalUnits.divide(BigDecimal(daysElapsed), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return CycleUsage(
            cycleStartDate = cycleStart,
            cycleEndDate = cycleEnd,
            totalUnitsSoFar = totalUnits,
            dailyAverage = dailyAverage,
            daysElapsed = daysElapsed,
            daysLeft = daysLeft
        )
    }

    fun computeCycleRange(cycleStartDay: Int, referenceDate: LocalDate): Pair<LocalDate, LocalDate> {
        val clampedDay = cycleStartDay.coerceIn(1, 28)

        val cycleStart: LocalDate
        val cycleEnd: LocalDate

        if (referenceDate.dayOfMonth >= clampedDay) {
            cycleStart = LocalDate(referenceDate.year, referenceDate.monthNumber, clampedDay)
            val nextMonthEpoch = cycleStart.plusMonths(1)
            cycleEnd = LocalDate(nextMonthEpoch.year, nextMonthEpoch.monthNumber, clampedDay).minusDays(1)
        } else {
            val prevMonthEpoch = LocalDate(referenceDate.year, referenceDate.monthNumber, 1).minusDays(1)
            cycleStart = LocalDate(prevMonthEpoch.year, prevMonthEpoch.monthNumber, clampedDay)
            cycleEnd = LocalDate(referenceDate.year, referenceDate.monthNumber, clampedDay).minusDays(1)
        }

        return Pair(cycleStart, cycleEnd)
    }

    private fun LocalDate.plusMonths(months: Int): LocalDate {
        var newMonth = this.monthNumber + months
        var newYear = this.year
        while (newMonth > 12) {
            newMonth -= 12
            newYear += 1
        }
        val clampedDay = this.dayOfMonth.coerceAtMost(daysInMonth(newYear, newMonth))
        return LocalDate(newYear, newMonth, clampedDay)
    }

    private fun LocalDate.minusDays(days: Int): LocalDate {
        return LocalDate.fromEpochDays(this.toEpochDays() - days)
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
