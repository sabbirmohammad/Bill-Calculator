package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Reading
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateDailyUsageUseCase @Inject constructor() {

    private val timeZone = TimeZone.of("Asia/Dhaka")

    operator fun invoke(
        readings: List<Reading>,
        multiplierFactor: BigDecimal = BigDecimal.ONE,
        costPerUnit: BigDecimal = BigDecimal.ZERO
    ): List<DailyUsage> {
        if (readings.size < 2) return emptyList()

        val sortedReadings = readings.sortedBy { it.recordedAt }
        val dailyMap = mutableMapOf<LocalDate, Pair<BigDecimal, Boolean>>() // date -> (units, isEstimated)

        for (i in 1 until sortedReadings.size) {
            val prev = sortedReadings[i - 1]
            val curr = sortedReadings[i]

            val prevDate = prev.recordedAt.toLocalDateTime(timeZone).date
            val currDate = curr.recordedAt.toLocalDateTime(timeZone).date

            val totalUnitsDelta = if (curr.isMeterReset) {
                // Meter was reset/replaced: consumption is from current reading value
                curr.readingValue.multiply(multiplierFactor)
            } else if (curr.readingValue >= prev.readingValue) {
                (curr.readingValue - prev.readingValue).multiply(multiplierFactor)
            } else {
                BigDecimal.ZERO
            }

            val daysBetween = daysBetween(prevDate, currDate)

            if (daysBetween <= 1) {
                val existing = dailyMap[currDate]
                val accumulated = (existing?.first ?: BigDecimal.ZERO) + totalUnitsDelta
                val isEstimated = existing?.second ?: curr.isEstimated
                dailyMap[currDate] = Pair(accumulated.setScale(2, RoundingMode.HALF_UP), isEstimated)
            } else {
                // Gap spreading: spread evenly across the days
                val daysCount = BigDecimal(daysBetween)
                val dailyPortion = totalUnitsDelta.divide(daysCount, 4, RoundingMode.HALF_UP)

                var stepDate = prevDate
                for (dayIndex in 1..daysBetween) {
                    stepDate = stepDate.plusDays(1)
                    val isGapDay = dayIndex < daysBetween || curr.isEstimated
                    dailyMap[stepDate] = Pair(dailyPortion.setScale(2, RoundingMode.HALF_UP), isGapDay)
                }
            }
        }

        return dailyMap.map { (date, pair) ->
            val units = pair.first.setScale(1, RoundingMode.HALF_UP)
            val cost = units.multiply(costPerUnit).setScale(2, RoundingMode.HALF_UP)
            DailyUsage(
                date = date,
                units = units,
                cost = cost,
                isEstimated = pair.second,
                isAnomaly = false
            )
        }.sortedBy { it.date }
    }

    private fun daysBetween(start: LocalDate, end: LocalDate): Int {
        val startEpoch = start.toEpochDays()
        val endEpoch = end.toEpochDays()
        return (endEpoch - startEpoch).coerceAtLeast(0)
    }

    private fun LocalDate.plusDays(days: Int): LocalDate {
        return LocalDate.fromEpochDays(this.toEpochDays() + days)
    }
}
