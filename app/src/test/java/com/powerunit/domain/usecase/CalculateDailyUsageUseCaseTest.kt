package com.powerunit.domain.usecase

import com.powerunit.core.common.DateUtils
import com.powerunit.domain.model.Reading
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculateDailyUsageUseCaseTest {

    private lateinit var useCase: CalculateDailyUsageUseCase

    @Before
    fun setUp() {
        useCase = CalculateDailyUsageUseCase()
    }

    @Test
    fun calculateDailyUsage_consecutiveDays_calculatesDirectDiff() {
        // Day 1: 1000.0
        // Day 2: 1010.5 (+10.5 units)
        // Day 3: 1022.0 (+11.5 units)
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("1000.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("1010.5"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0)
            ),
            Reading(
                id = 3, meterId = 1, readingValue = BigDecimal("1022.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 3, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 3, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 3, 9, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(2, result.size)
        assertEquals(LocalDate(2026, 3, 2), result[0].date)
        assertEquals(BigDecimal("10.5"), result[0].units)
        assertFalse(result[0].isEstimated)

        assertEquals(LocalDate(2026, 3, 3), result[1].date)
        assertEquals(BigDecimal("11.5"), result[1].units)
        assertFalse(result[1].isEstimated)
    }

    @Test
    fun calculateDailyUsage_gapSpreading_spreadsEvenlyAndMarksEstimated() {
        // Day 1 (Mar 1): 1000.0
        // Gap: Mar 2 and Mar 3 missed
        // Day 4 (Mar 4): 1030.0 -> total delta 30.0 units over 3 days (10.0 units/day)
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("1000.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("1030.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 4, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 4, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 4, 9, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(3, result.size)
        // Mar 2
        assertEquals(LocalDate(2026, 3, 2), result[0].date)
        assertEquals(BigDecimal("10.0"), result[0].units)
        assertTrue(result[0].isEstimated)

        // Mar 3
        assertEquals(LocalDate(2026, 3, 3), result[1].date)
        assertEquals(BigDecimal("10.0"), result[1].units)
        assertTrue(result[1].isEstimated)

        // Mar 4
        assertEquals(LocalDate(2026, 3, 4), result[2].date)
        assertEquals(BigDecimal("10.0"), result[2].units)
    }

    @Test
    fun calculateDailyUsage_meterReset_handlesNewMeterReading() {
        // Day 1: 9999.0
        // Day 2: New meter replaced, start reading is 5.0, marked isMeterReset = true
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("9999.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("5.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                isMeterReset = true,
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(1, result.size)
        assertEquals(LocalDate(2026, 3, 2), result[0].date)
        assertEquals(BigDecimal("5.0"), result[0].units)
    }

    @Test
    fun calculateDailyUsage_multiplierFactor_scalesProperly() {
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("100.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("102.5"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0)
            )
        )

        val result = useCase(readings, multiplierFactor = BigDecimal("10.0"))

        assertEquals(1, result.size)
        assertEquals(BigDecimal("25.0"), result[0].units)
    }

    @Test
    fun calculateDailyUsage_hugeGap_strictlyConservesTotalDelta() {
        // Delta = 20.0 units over 7 days. Under naive flat division, 2.9 * 7 = 20.3 (0.3 leaked).
        // Under our conserved Hamilton distribution, sum must be EXACTLY 20.0 units!
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("1000.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("1020.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 8, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 8, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 8, 9, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(7, result.size)
        val sumUnits = result.fold(BigDecimal.ZERO) { acc, u -> acc + u.units }
        assertEquals(BigDecimal("20.0"), sumUnits)

        // 30-day huge gap test (100.0 units over 30 days)
        val monthReadings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("5000.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 4, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 4, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 4, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("5100.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 5, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 5, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 5, 1, 9, 0)
            )
        )
        val monthResult = useCase(monthReadings)
        assertEquals(30, monthResult.size)
        val monthSum = monthResult.fold(BigDecimal.ZERO) { acc, u -> acc + u.units }
        assertEquals(BigDecimal("100.0"), monthSum)
    }

    @Test
    fun calculateDailyUsage_meterRollover_detectsDialWrapAround() {
        // 5-digit meter (capacity 100,000): reads 99995.0 on Mar 1, rolls over to 00010.0 on Mar 2
        // True consumption = (100000 - 99995) + 10 = 15.0 units
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("99995.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 9, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("10.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 2, 9, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(1, result.size)
        assertEquals(BigDecimal("15.0"), result[0].units)
    }

    @Test
    fun calculateDailyUsage_multipleReadingsSameDay_accumulatesCorrectly() {
        // Morning reading (1000.0), midday reading (1004.0, +4 units), night reading (1009.5, +5.5 units)
        // Total for that day should be 9.5 units
        val readings = listOf(
            Reading(
                id = 1, meterId = 1, readingValue = BigDecimal("1000.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 8, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 8, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 8, 0)
            ),
            Reading(
                id = 2, meterId = 1, readingValue = BigDecimal("1004.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 14, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 14, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 14, 0)
            ),
            Reading(
                id = 3, meterId = 1, readingValue = BigDecimal("1009.5"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 22, 0),
                createdAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 22, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 3, 1, 22, 0)
            )
        )

        val result = useCase(readings)

        assertEquals(1, result.size)
        assertEquals(LocalDate(2026, 3, 1), result[0].date)
        assertEquals(BigDecimal("9.5"), result[0].units)
    }
}
