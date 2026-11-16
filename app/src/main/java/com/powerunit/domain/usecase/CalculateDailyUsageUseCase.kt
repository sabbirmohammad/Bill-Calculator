package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Reading
import kotlinx.datetime.DayOfWeek
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

        // Precompute weekday profile if sufficient historical non-estimated readings exist (>= 14 points)
        val weekdayWeights = computeWeekdayWeights(sortedReadings)

        for (i in 1 until sortedReadings.size) {
            val prev = sortedReadings[i - 1]
            val curr = sortedReadings[i]

            val prevDate = prev.recordedAt.toLocalDateTime(timeZone).date
            val currDate = curr.recordedAt.toLocalDateTime(timeZone).date

            val totalUnitsDelta = if (curr.isMeterReset) {
                // Meter was reset/replaced: consumption measured from new baseline
                curr.readingValue.multiply(multiplierFactor)
            } else if (curr.readingValue >= prev.readingValue) {
                (curr.readingValue - prev.readingValue).multiply(multiplierFactor)
            } else {
                // Check for meter rollover (e.g. 99999.9 -> 00005.0)
                detectRolloverDelta(prev.readingValue, curr.readingValue, multiplierFactor) ?: BigDecimal.ZERO
            }

            val daysBetween = daysBetween(prevDate, currDate)

            if (daysBetween <= 1) {
                val existing = dailyMap[currDate]
                val accumulated = (existing?.first ?: BigDecimal.ZERO) + totalUnitsDelta
                val isEstimated = existing?.second ?: curr.isEstimated
                dailyMap[currDate] = Pair(accumulated.setScale(2, RoundingMode.HALF_UP), isEstimated)
            } else {
                // Gap spreading: generate target dates
                val gapDates = mutableListOf<LocalDate>()
                var step = prevDate
                for (dayIndex in 1..daysBetween) {
                    step = step.plusDays(1)
                    gapDates.add(step)
                }

                // Distribute units across gap with strict conservation of total delta
                val distributed = distributeGapConsumption(totalUnitsDelta, gapDates, weekdayWeights)

                for ((dayIndex, date) in gapDates.withIndex()) {
                    val portion = distributed[date] ?: BigDecimal.ZERO
                    val isGapDay = (dayIndex < daysBetween - 1) || curr.isEstimated
                    dailyMap[date] = Pair(portion, isGapDay)
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

    /**
     * Distributes totalUnitsDelta across the given dates such that the sum of distributed units
     * strictly and exactly equals totalUnitsDelta (Hamilton / Largest Remainder method).
     */
    private fun distributeGapConsumption(
        totalUnits: BigDecimal,
        dates: List<LocalDate>,
        weekdayWeights: Map<DayOfWeek, Double>?
    ): Map<LocalDate, BigDecimal> {
        val count = dates.size
        if (count == 0) return emptyMap()
        if (count == 1) return mapOf(dates.first() to totalUnits.setScale(1, RoundingMode.HALF_UP))
        if (totalUnits <= BigDecimal.ZERO) {
            return dates.associateWith { BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP) }
        }

        val totalScaledTenths = totalUnits.multiply(BigDecimal("10")).setScale(0, RoundingMode.HALF_UP).toInt()

        val proportions = if (weekdayWeights != null && weekdayWeights.values.any { it > 0.0 }) {
            val totalWeight = dates.sumOf { weekdayWeights[it.dayOfWeek] ?: 1.0 }.coerceAtLeast(0.001)
            dates.map { (weekdayWeights[it.dayOfWeek] ?: 1.0) / totalWeight }
        } else {
            val uniform = 1.0 / count
            dates.map { uniform }
        }

        // Base integer tenths per day
        val rawTenths = proportions.map { it * totalScaledTenths }
        val baseTenths = rawTenths.map { it.toInt() }.toMutableList()
        val remainders = rawTenths.mapIndexed { idx, raw -> Pair(idx, raw - baseTenths[idx]) }
            .sortedByDescending { it.second }

        var tenthsToDistribute = totalScaledTenths - baseTenths.sum()
        var rIdx = 0
        while (tenthsToDistribute > 0 && rIdx < remainders.size) {
            val targetDayIdx = remainders[rIdx].first
            baseTenths[targetDayIdx] += 1
            tenthsToDistribute--
            rIdx++
        }

        val result = mutableMapOf<LocalDate, BigDecimal>()
        for (i in dates.indices) {
            val units = BigDecimal(baseTenths[i]).divide(BigDecimal("10"), 1, RoundingMode.HALF_UP)
            result[dates[i]] = units
        }
        return result
    }

    /**
     * Detects physical meter dial rollover (e.g. 99999.9 -> 00004.5) across standard 4, 5, or 6 digit dials.
     */
    private fun detectRolloverDelta(
        prevReading: BigDecimal,
        currReading: BigDecimal,
        multiplier: BigDecimal
    ): BigDecimal? {
        val rolloverCapacities = listOf(
            BigDecimal("10000"),
            BigDecimal("100000"),
            BigDecimal("1000000")
        )
        for (capacity in rolloverCapacities) {
            val threshold = capacity.multiply(BigDecimal("0.9"))
            if (prevReading >= threshold && currReading < capacity.multiply(BigDecimal("0.2"))) {
                val rolloverDelta = (capacity - prevReading + currReading).multiply(multiplier)
                if (rolloverDelta > BigDecimal.ZERO && rolloverDelta < capacity.multiply(BigDecimal("0.3"))) {
                    return rolloverDelta
                }
            }
        }
        return null
    }

    /**
     * Computes empirical day-of-week relative consumption weights if >= 14 non-estimated historical points exist.
     */
    private fun computeWeekdayWeights(readings: List<Reading>): Map<DayOfWeek, Double>? {
        if (readings.size < 15) return null

        val weekdayDeltas = mutableMapOf<DayOfWeek, MutableList<Double>>()
        DayOfWeek.values().forEach { weekdayDeltas[it] = mutableListOf() }

        for (i in 1 until readings.size) {
            val prev = readings[i - 1]
            val curr = readings[i]
            if (curr.isEstimated || curr.isMeterReset) continue

            val prevDate = prev.recordedAt.toLocalDateTime(timeZone).date
            val currDate = curr.recordedAt.toLocalDateTime(timeZone).date
            if (daysBetween(prevDate, currDate) == 1 && curr.readingValue >= prev.readingValue) {
                val delta = (curr.readingValue - prev.readingValue).toDouble()
                weekdayDeltas[currDate.dayOfWeek]?.add(delta)
            }
        }

        val totalPoints = weekdayDeltas.values.sumOf { it.size }
        if (totalPoints < 14) return null

        val averages = weekdayDeltas.mapValues { (_, list) ->
            if (list.isNotEmpty()) list.average() else 1.0
        }
        return averages
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
