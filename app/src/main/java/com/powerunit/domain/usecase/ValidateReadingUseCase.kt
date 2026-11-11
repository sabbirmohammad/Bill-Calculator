package com.powerunit.domain.usecase

import com.powerunit.domain.model.ReadingValidationResult
import kotlinx.datetime.Instant
import java.math.BigDecimal
import javax.inject.Inject

class ValidateReadingUseCase @Inject constructor() {

    operator fun invoke(
        newReadingValue: BigDecimal,
        recordedAt: Instant,
        currentInstant: Instant,
        previousReadingValue: BigDecimal?,
        isMeterReset: Boolean,
        average30DayUnits: BigDecimal?
    ): ReadingValidationResult {
        // Rule 1: No future dates allowed
        if (recordedAt > currentInstant) {
            return ReadingValidationResult.FutureDateError()
        }

        // Rule 2: Reading must be >= previous reading unless marked as meter reset
        if (previousReadingValue != null && !isMeterReset) {
            if (newReadingValue < previousReadingValue) {
                return ReadingValidationResult.LowerThanPreviousWarning(previousReadingValue)
            }

            // Rule 3: Consumption spike warning (more than 3x the 30-day average)
            if (average30DayUnits != null && average30DayUnits > BigDecimal.ZERO) {
                val consumption = newReadingValue - previousReadingValue
                val tripleAverage = average30DayUnits.multiply(BigDecimal("3.0"))
                if (consumption > tripleAverage && consumption > BigDecimal("10.0")) {
                    return ReadingValidationResult.HighConsumptionWarning(
                        consumedUnits = consumption,
                        averageUnits = average30DayUnits
                    )
                }
            }
        }

        return ReadingValidationResult.Valid
    }
}
