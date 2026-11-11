package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.ReadingValidationResult
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class OtherUseCasesTest {

    @Test
    fun compareWithPrevious_computesPercentageChangeAccurately() {
        val compare = CompareWithPreviousUseCase()

        // 100 to 120 -> +20.0%
        val res1 = compare(BigDecimal("120.0"), BigDecimal("100.0"))
        assertEquals(20.0, res1.percentChange, 0.01)

        // 100 to 80 -> -20.0%
        val res2 = compare(BigDecimal("80.0"), BigDecimal("100.0"))
        assertEquals(-20.0, res2.percentChange, 0.01)

        // Previous was 0, now 50 -> +100.0%
        val res3 = compare(BigDecimal("50.0"), BigDecimal.ZERO)
        assertEquals(100.0, res3.percentChange, 0.01)
    }

    @Test
    fun detectAnomalies_flagsOutliersAboveTwoStandardDeviations() {
        val detect = DetectAnomaliesUseCase()

        // 20 normal days of 10.0 units, then 1 day with 50.0 units (severe outlier)
        val normalDays = (1..20).map {
            DailyUsage(LocalDate(2026, 3, it), BigDecimal("10.0"), BigDecimal("50.0"))
        }
        val spikeDay = DailyUsage(LocalDate(2026, 3, 21), BigDecimal("50.0"), BigDecimal("250.0"))

        val allDays = normalDays + spikeDay
        val flagged = detect(allDays)

        assertFalse(flagged[0].isAnomaly)
        assertTrue(flagged.last().isAnomaly)
    }

    @Test
    fun calculateMetrics_computesMetricsAndCo2() {
        val metricsUseCase = CalculateMetricsUseCase()

        // 100 units, 600 BDT cost, 10 days elapsed
        val metrics = metricsUseCase(
            totalUnits = BigDecimal("100.0"),
            totalCost = BigDecimal("600.00"),
            daysElapsed = 10,
            emissionFactorKgPerKwh = 0.55
        )

        assertEquals(BigDecimal("60.00"), metrics.costPerDay)
        assertEquals(BigDecimal("6.00"), metrics.costPerUnitEffective)
        assertEquals(BigDecimal("55.00"), metrics.co2EstimateKg) // 100 * 0.55
    }

    @Test
    fun calculatePrepaidStatus_computesRemainingDaysAndAlert() {
        val prepaidUseCase = CalculatePrepaidStatusUseCase()

        // Balance 500 BDT, daily cost 50 BDT -> 10 days remaining (safe, no alert)
        val safeStatus = prepaidUseCase(
            currentBalanceBdt = BigDecimal("500.00"),
            averageDailyCostBdt = BigDecimal("50.00"),
            averageCostPerUnitBdt = BigDecimal("6.00")
        )
        assertEquals(10, safeStatus.estimatedDaysRemaining)
        assertFalse(safeStatus.isRechargeSoonAlert)

        // Balance 100 BDT, daily cost 50 BDT -> 2 days remaining (triggers alert)
        val lowStatus = prepaidUseCase(
            currentBalanceBdt = BigDecimal("100.00"),
            averageDailyCostBdt = BigDecimal("50.00"),
            averageCostPerUnitBdt = BigDecimal("6.00")
        )
        assertEquals(2, lowStatus.estimatedDaysRemaining)
        assertTrue(lowStatus.isRechargeSoonAlert)
    }

    @Test
    fun validateReading_detectsValidationErrorsAndWarnings() {
        val validate = ValidateReadingUseCase()
        val now = Instant.fromEpochMilliseconds(1710000000000L)

        // Future date check
        val futureInstant = Instant.fromEpochMilliseconds(1710000001000L)
        val futureResult = validate(
            newReadingValue = BigDecimal("1050.0"),
            recordedAt = futureInstant,
            currentInstant = now,
            previousReadingValue = BigDecimal("1000.0"),
            isMeterReset = false,
            average30DayUnits = BigDecimal("10.0")
        )
        assertTrue(futureResult is ReadingValidationResult.FutureDateError)

        // Lower than previous without meter reset
        val lowerResult = validate(
            newReadingValue = BigDecimal("950.0"),
            recordedAt = now,
            currentInstant = now,
            previousReadingValue = BigDecimal("1000.0"),
            isMeterReset = false,
            average30DayUnits = BigDecimal("10.0")
        )
        assertTrue(lowerResult is ReadingValidationResult.LowerThanPreviousWarning)

        // Lower than previous WITH meter reset flag -> should be Valid
        val resetResult = validate(
            newReadingValue = BigDecimal("50.0"),
            recordedAt = now,
            currentInstant = now,
            previousReadingValue = BigDecimal("1000.0"),
            isMeterReset = true,
            average30DayUnits = BigDecimal("10.0")
        )
        assertTrue(resetResult is ReadingValidationResult.Valid)

        // Consumption spike > 3x average
        // 30-day average is 10.0, current consumption = 1050 - 1000 = 50.0 (> 30.0)
        val spikeResult = validate(
            newReadingValue = BigDecimal("1050.0"),
            recordedAt = now,
            currentInstant = now,
            previousReadingValue = BigDecimal("1000.0"),
            isMeterReset = false,
            average30DayUnits = BigDecimal("10.0")
        )
        assertTrue(spikeResult is ReadingValidationResult.HighConsumptionWarning)
    }

    @Test
    fun domainPackage_hasZeroAndroidDependencies() {
        val domainClasses = listOf(
            com.powerunit.domain.model.Meter::class.java,
            com.powerunit.domain.model.Reading::class.java,
            com.powerunit.domain.model.DailyUsage::class.java,
            com.powerunit.domain.model.Tariff::class.java,
            com.powerunit.domain.model.BillBreakdown::class.java,
            com.powerunit.domain.model.CycleUsage::class.java,
            com.powerunit.domain.model.MonthEndForecast::class.java,
            com.powerunit.domain.model.PeriodComparison::class.java,
            com.powerunit.domain.model.PrepaidStatus::class.java,
            com.powerunit.domain.model.ReadingValidationResult::class.java,
            CalculateBillUseCase::class.java,
            CalculateDailyUsageUseCase::class.java,
            CalculateCycleUsageUseCase::class.java,
            ForecastMonthEndUseCase::class.java,
            CompareWithPreviousUseCase::class.java,
            DetectAnomaliesUseCase::class.java,
            CalculateMetricsUseCase::class.java,
            CalculatePrepaidStatusUseCase::class.java,
            ValidateReadingUseCase::class.java
        )

        for (clazz in domainClasses) {
            val packageName = clazz.`package`?.name ?: ""
            assertTrue(packageName.startsWith("com.powerunit.domain"))
            for (field in clazz.declaredFields) {
                val fieldType = field.type.name
                assertFalse("Field in domain should not depend on android: $fieldType", fieldType.startsWith("android."))
            }
        }
    }
}
