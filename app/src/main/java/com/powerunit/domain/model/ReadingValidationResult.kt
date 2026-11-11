package com.powerunit.domain.model

import java.math.BigDecimal

sealed interface ReadingValidationResult {
    data object Valid : ReadingValidationResult

    data class FutureDateError(val message: String = "Reading date cannot be in the future.") : ReadingValidationResult

    data class LowerThanPreviousWarning(
        val previousValue: BigDecimal,
        val message: String = "This is lower than your last reading ($previousValue). Did the meter reset or get replaced?"
    ) : ReadingValidationResult

    data class HighConsumptionWarning(
        val consumedUnits: BigDecimal,
        val averageUnits: BigDecimal,
        val message: String = "High consumption detected ($consumedUnits units is more than 3x your 30-day average of $averageUnits units). Please verify the reading."
    ) : ReadingValidationResult
}
