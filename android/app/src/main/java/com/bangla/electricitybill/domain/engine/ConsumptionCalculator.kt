package com.bangla.electricitybill.domain.engine

import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.MeterReading
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object ConsumptionCalculator {

    /**
     * Calculates consumption between previous and current readings.
     * Returns the consumption in kWh with exact precision.
     */
    fun calculateConsumption(
        previousReading: BigDecimal,
        currentReading: BigDecimal,
        isRollover: Boolean = false,
        blackDigitCapacity: Int = 5
    ): Result<BigDecimal> {
        val prev = BigDecimalMath.toReadingScale(previousReading)
        val curr = BigDecimalMath.toReadingScale(currentReading)

        if (!isRollover) {
            val delta = curr.subtract(prev)
            return if (delta < BigDecimal.ZERO) {
                Result.failure(
                    IllegalArgumentException(
                        "বর্তমান রিডিং ($curr) পূর্ববর্তী রিডিং ($prev) এর চেয়ে কম হতে পারবে না (Current reading cannot be lower than previous reading without rollover)"
                    )
                )
            } else {
                Result.success(delta)
            }
        } else {
            // Rollover calculation: (10^N - prev) + curr with exact BigDecimal power
            val capacityLimit = BigDecimal.TEN.pow(blackDigitCapacity)
                .setScale(BigDecimalMath.READING_SCALE, RoundingMode.UNNECESSARY)

            if (curr >= prev) {
                return Result.failure(
                    IllegalArgumentException("রোলওভার কেবল তখনই প্রযোজ্য যখন বর্তমান রিডিং পূর্বের চেয়ে কম (Rollover only applies when current reading is smaller than previous)")
                )
            }

            val beforeRollover = capacityLimit.subtract(prev)
            val totalRolloverConsumption = beforeRollover.add(curr)
            return Result.success(totalRolloverConsumption)
        }
    }

    /**
     * Calculates elapsed days between readings.
     * Minimum 1 day for non-zero time difference, or 1 if same date.
     */
    fun calculateElapsedDays(prevReading: MeterReading, currReading: MeterReading): Long {
        val days = DateUtils.daysBetween(prevReading.readingDateTime, currReading.readingDateTime)
        return if (days <= 0L) 1L else days
    }

    /**
     * Calculates average daily consumption = total interval units / elapsed days.
     */
    fun calculateAverageDaily(consumption: BigDecimal, elapsedDays: Long): BigDecimal {
        if (elapsedDays <= 0L) return consumption
        return consumption.divide(
            BigDecimal(elapsedDays),
            BigDecimalMath.CALCULATION_SCALE,
            RoundingMode.HALF_UP
        )
    }
}
