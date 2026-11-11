package com.powerunit.domain.usecase

import com.powerunit.domain.model.PeriodComparison
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CompareWithPreviousUseCase @Inject constructor() {

    operator fun invoke(currentUnits: BigDecimal, previousUnits: BigDecimal): PeriodComparison {
        val curr = currentUnits.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
        val prev = previousUnits.coerceAtLeast(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)

        val percentChange = if (prev.compareTo(BigDecimal.ZERO) == 0) {
            if (curr.compareTo(BigDecimal.ZERO) == 0) 0.0 else 100.0
        } else {
            val delta = curr - prev
            val ratio = delta.divide(prev, 4, RoundingMode.HALF_UP)
            ratio.multiply(BigDecimal("100")).toDouble()
        }

        return PeriodComparison(
            currentUnits = curr,
            previousUnits = prev,
            percentChange = (percentChange * 10.0).toLong() / 10.0 // Rounded to 1 decimal place
        )
    }
}
