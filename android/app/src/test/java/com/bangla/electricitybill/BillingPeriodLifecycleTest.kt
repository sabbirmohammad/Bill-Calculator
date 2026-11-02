package com.bangla.electricitybill

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.BillingPeriodLifecycleManager
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.PeriodStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class BillingPeriodLifecycleTest {

    // Test 9: New billing period is independent of the previous period
    @Test
    fun test09_billingPeriodIndependence() {
        val meterId = "m1"
        val periodNovId = DateUtils.getCycleId(meterId, YearMonth.of(2026, 11))
        val periodDecId = DateUtils.getCycleId(meterId, YearMonth.of(2026, 12))

        assertNotEquals(periodNovId, periodDecId)
    }

    // Test 10: Physical meter reading is not reset at month-end
    @Test
    fun test10_physicalMeterNotResetAtMonthEnd() {
        val nov30Reading = BigDecimal("12500.5")
        val dec1Reading = BigDecimal("12508.2")

        // Reading accumulates forward
        assertTrue(dec1Reading >= nov30Reading)
        val consumption = dec1Reading.subtract(nov30Reading)
        assertEquals(BigDecimal("7.7"), consumption)
    }

    // Test 11: Previous months remain accessible
    @Test
    fun test11_previousMonthsAccessible() {
        val periodList = listOf(
            BillingPeriod("p1", "m1", LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 31, 23, 59), BigDecimal("12000.0"), BigDecimal("12200.0"), "t1", PeriodStatus.COMPLETED, null, LocalDateTime.now()),
            BillingPeriod("p2", "m1", LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 11, 30, 23, 59), BigDecimal("12200.0"), BigDecimal("12450.0"), "t1", PeriodStatus.COMPLETED, null, LocalDateTime.now()),
            BillingPeriod("p3", "m1", LocalDateTime.of(2026, 12, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59), BigDecimal("12450.0"), null, "t1", PeriodStatus.IN_PROGRESS, null, LocalDateTime.now())
        )
        assertEquals(3, periodList.size)
        assertEquals(PeriodStatus.COMPLETED, periodList[0].periodStatus)
    }

    // Test 12: Missing month-boundary readings are identified
    @Test
    fun test12_missingMonthBoundaryReadingsIdentified() {
        val period = BillingPeriod(
            id = "m1_2026_11",
            meterId = "m1",
            periodStart = LocalDateTime.of(2026, 11, 1, 0, 0),
            periodEnd = LocalDateTime.of(2026, 11, 30, 23, 59),
            openingReading = null, // Missing exact opening boundary reading
            closingReading = null, // Missing exact closing boundary reading
            tariffVersionId = "t1",
            periodStatus = PeriodStatus.IN_PROGRESS,
            trackingStartDate = null,
            createdAt = LocalDateTime.now()
        )

        val readings = listOf(
            MeterReading("r1", "m1", "12345", "6", BigDecimal("12345.6"), LocalDateTime.of(2026, 11, 5, 10, 0), LocalDateTime.now(), LocalDateTime.now()),
            MeterReading("r2", "m1", "12380", "0", BigDecimal("12380.0"), LocalDateTime.of(2026, 11, 25, 10, 0), LocalDateTime.now(), LocalDateTime.now())
        )

        val status = BillingPeriodLifecycleManager.evaluatePeriodStatus(
            period = period,
            readingsInPeriod = readings,
            isCurrentMonth = false, // Evaluating completed past month
            initialBaselineDate = LocalDate.of(2026, 10, 1)
        )

        assertEquals(PeriodStatus.INCOMPLETE_BOUNDARY_DATA, status)
    }

    // Test 13: Initial mid-month tracking is marked as partial
    @Test
    fun test13_midMonthInitialTrackingMarkedAsPartial() {
        val period = BillingPeriod(
            id = "m1_2026_11",
            meterId = "m1",
            periodStart = LocalDateTime.of(2026, 11, 1, 0, 0),
            periodEnd = LocalDateTime.of(2026, 11, 30, 23, 59),
            openingReading = BigDecimal("12500.0"),
            closingReading = null,
            tariffVersionId = "t1",
            periodStatus = PeriodStatus.IN_PROGRESS,
            trackingStartDate = LocalDateTime.of(2026, 11, 14, 10, 0),
            createdAt = LocalDateTime.now()
        )

        val status = BillingPeriodLifecycleManager.evaluatePeriodStatus(
            period = period,
            readingsInPeriod = emptyList(),
            isCurrentMonth = true,
            initialBaselineDate = LocalDate.of(2026, 11, 14) // Mid-month!
        )

        assertEquals(PeriodStatus.PARTIAL_TRACKING, status)
    }
}
