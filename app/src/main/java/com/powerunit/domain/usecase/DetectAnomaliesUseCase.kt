package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.abs

class DetectAnomaliesUseCase @Inject constructor() {

    operator fun invoke(dailyUsages: List<DailyUsage>): List<DailyUsage> {
        if (dailyUsages.size < 3) return dailyUsages

        return dailyUsages.mapIndexed { index, targetUsage ->
            // Exclude target day under test and exclude synthetic estimated/interpolated days from baseline
            val baseline = dailyUsages
                .filterIndexed { idx, usage -> idx != index && !usage.isEstimated }
                .takeLast(30)

            if (baseline.size < 3) {
                // Insufficient non-estimated baseline history to declare anomaly
                targetUsage.copy(isAnomaly = false)
            } else {
                val baselineUnits = baseline.map { it.units.toDouble() }
                val targetUnits = targetUsage.units.toDouble()

                val median = calculateMedian(baselineUnits)
                val absoluteDeviations = baselineUnits.map { abs(it - median) }
                val mad = calculateMedian(absoluteDeviations)

                // Scale factor for normal distribution equivalence
                val madSigma = 1.4826 * mad

                // Fallback for zero-MAD situations (e.g. constant usage across most baseline days)
                val meanAd = absoluteDeviations.average()
                val minEffectiveSigma = (0.2 * median).coerceAtLeast(0.5)
                val effectiveSigma = maxOf(madSigma, meanAd, minEffectiveSigma)

                // Robust anomaly score
                val robustZScore = (targetUnits - median) / effectiveSigma

                // Anomaly condition:
                // 1. Robust Z-score > 2.5 (strong statistical outlier)
                // 2. Or relative surge >= 2.5x median with at least 2.0 kWh increase (sensitive to low-usage rural households)
                val isStatisticalOutlier = robustZScore > 2.5
                val isLowUsageSurge = median > 0.0 && targetUnits >= (2.5 * median) && (targetUnits - median) >= 2.0

                val isAnomaly = isStatisticalOutlier || isLowUsageSurge
                targetUsage.copy(isAnomaly = isAnomaly)
            }
        }
    }

    private fun calculateMedian(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val n = sorted.size
        return if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }
    }
}
