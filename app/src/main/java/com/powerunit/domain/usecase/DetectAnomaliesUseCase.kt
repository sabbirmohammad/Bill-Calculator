package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.sqrt

class DetectAnomaliesUseCase @Inject constructor() {

    operator fun invoke(dailyUsages: List<DailyUsage>): List<DailyUsage> {
        if (dailyUsages.size < 3) return dailyUsages

        val last30Days = dailyUsages.takeLast(30)
        val n = last30Days.size.toDouble()

        val mean = last30Days.map { it.units.toDouble() }.sum() / n
        val variance = last30Days.map {
            val diff = it.units.toDouble() - mean
            diff * diff
        }.sum() / n

        val stdDev = sqrt(variance)
        val anomalyThreshold = mean + (2.0 * stdDev)

        return dailyUsages.map { usage ->
            val isAnomaly = usage.units.toDouble() > anomalyThreshold && stdDev > 0.5
            usage.copy(isAnomaly = isAnomaly)
        }
    }
}
