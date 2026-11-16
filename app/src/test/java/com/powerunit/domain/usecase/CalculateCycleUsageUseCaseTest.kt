package com.powerunit.domain.usecase

import com.powerunit.domain.model.DailyUsage
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculateCycleUsageUseCaseTest {

    private lateinit var useCase: CalculateCycleUsageUseCase

    @Before
    fun setUp() {
        useCase = CalculateCycleUsageUseCase()
    }

    @Test
    fun computeCycleRange_cycleStartDay1() {
        val today = LocalDate(2026, 3, 10)
        val (start, end) = useCase.computeCycleRange(1, today)

        assertEquals(LocalDate(2026, 3, 1), start)
        assertEquals(LocalDate(2026, 3, 31), end)
    }

    @Test
    fun computeCycleRange_cycleStartDay15_whenTodayIsPast15th() {
        val today = LocalDate(2026, 3, 20)
        val (start, end) = useCase.computeCycleRange(15, today)

        assertEquals(LocalDate(2026, 3, 15), start)
        assertEquals(LocalDate(2026, 4, 14), end)
    }

    @Test
    fun computeCycleRange_cycleStartDay15_whenTodayIsBefore15th() {
        val today = LocalDate(2026, 3, 10)
        val (start, end) = useCase.computeCycleRange(15, today)

        assertEquals(LocalDate(2026, 2, 15), start)
        assertEquals(LocalDate(2026, 3, 14), end)
    }

    @Test
    fun calculateCycleUsage_computesTotalsAndAverage() {
        val today = LocalDate(2026, 3, 5)
        val usages = listOf(
            DailyUsage(LocalDate(2026, 3, 1), BigDecimal("10.0"), BigDecimal("50.0")),
            DailyUsage(LocalDate(2026, 3, 2), BigDecimal("12.0"), BigDecimal("60.0")),
            DailyUsage(LocalDate(2026, 3, 3), BigDecimal("8.0"), BigDecimal("40.0")),
            DailyUsage(LocalDate(2026, 3, 4), BigDecimal("14.0"), BigDecimal("70.0")),
            DailyUsage(LocalDate(2026, 3, 5), BigDecimal("16.0"), BigDecimal("80.0"))
        )

        val cycle = useCase(usages, cycleStartDay = 1, today = today)

        assertEquals(LocalDate(2026, 3, 1), cycle.cycleStartDate)
        assertEquals(LocalDate(2026, 3, 31), cycle.cycleEndDate)
        assertEquals(BigDecimal("60.0"), cycle.totalUnitsSoFar) // 10+12+8+14+16
        assertEquals(5, cycle.daysElapsed)
        assertEquals(26, cycle.daysLeft) // 31 - 5 = 26
        assertEquals(BigDecimal("12.00"), cycle.dailyAverage) // 60 / 5 = 12.00
    }

    @Test
    fun computeCycleRange_shortMonthHandling_cycleStartDay31() {
        // Today is April 15 (April has 30 days, so start day 31 clamps to 30)
        // Previous month is March (31 days, start day 31)
        val today = LocalDate(2026, 4, 15)
        val (start, end) = useCase.computeCycleRange(31, today)

        assertEquals(LocalDate(2026, 3, 31), start)
        assertEquals(LocalDate(2026, 4, 29), end)
    }

    @Test
    fun computeCycleRange_leapYearHandling_february29() {
        // Year 2028 is a leap year (February has 29 days)
        val today = LocalDate(2028, 2, 10)
        val (start, end) = useCase.computeCycleRange(1, today)

        assertEquals(LocalDate(2028, 2, 1), start)
        assertEquals(LocalDate(2028, 2, 29), end)
    }
}
