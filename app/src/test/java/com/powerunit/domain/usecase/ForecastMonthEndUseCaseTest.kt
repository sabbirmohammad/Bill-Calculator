package com.powerunit.domain.usecase

import com.powerunit.domain.model.CycleUsage
import com.powerunit.domain.model.DailyUsage
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ForecastMonthEndUseCaseTest {

    private lateinit var useCase: ForecastMonthEndUseCase
    private lateinit var calculateBillUseCase: CalculateBillUseCase
    private lateinit var tariff: Tariff

    @Before
    fun setUp() {
        calculateBillUseCase = CalculateBillUseCase()
        useCase = ForecastMonthEndUseCase(calculateBillUseCase)

        tariff = Tariff(
            id = 1,
            name = "Flat Rate Test Tariff",
            effectiveFrom = Instant.fromEpochMilliseconds(0),
            vatPercent = BigDecimal("5.0"),
            serviceChargeFlat = BigDecimal("10.00"),
            demandChargeFlat = BigDecimal("35.00"),
            isActive = true,
            slabs = listOf(
                TariffSlab(id = 1, tariffId = 1, fromUnit = BigDecimal("0"), toUnit = null, ratePerUnit = BigDecimal("6.00"))
            )
        )
    }

    @Test
    fun forecastMonthEnd_zeroDaysLeft_returnsCurrentValues() {
        val cycle = CycleUsage(
            cycleStartDate = LocalDate(2026, 3, 1),
            cycleEndDate = LocalDate(2026, 3, 31),
            totalUnitsSoFar = BigDecimal("300.0"),
            dailyAverage = BigDecimal("10.00"),
            daysElapsed = 31,
            daysLeft = 0
        )

        val forecast = useCase(emptyList(), cycle, tariff)

        assertEquals(BigDecimal("300.0"), forecast.expectedUnits)
        assertEquals(BigDecimal("300.0"), forecast.lowUnits)
        assertEquals(BigDecimal("300.0"), forecast.highUnits)
        assertEquals(0, forecast.daysLeft)
    }

    @Test
    fun forecastMonthEnd_daysRemaining_computesWeightedRange() {
        val cycle = CycleUsage(
            cycleStartDate = LocalDate(2026, 3, 1),
            cycleEndDate = LocalDate(2026, 3, 31),
            totalUnitsSoFar = BigDecimal("100.0"),
            dailyAverage = BigDecimal("10.00"),
            daysElapsed = 10,
            daysLeft = 21
        )

        // 7 days of 10.0 units/day
        val usages = (1..7).map {
            DailyUsage(LocalDate(2026, 3, it), BigDecimal("10.0"), BigDecimal("60.0"))
        }

        val forecast = useCase(usages, cycle, tariff)

        // avg7 = 10.0, cycleAvg = 10.0 -> weightedRate = 10.0
        // expectedAdditional = 10.0 * 21 = 210.0
        // expectedUnits = 100.0 + 210.0 = 310.0
        assertEquals(BigDecimal("310.0"), forecast.expectedUnits)
        assertTrue(forecast.lowUnits < forecast.expectedUnits)
        assertTrue(forecast.highUnits > forecast.expectedUnits)
        assertTrue(forecast.lowBill < forecast.expectedBill)
        assertTrue(forecast.highBill > forecast.expectedBill)
    }
}
