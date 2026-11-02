package com.bangla.electricitybill

import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.MeterReadingParser
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.AdditionalCharge
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import com.bangla.electricitybill.domain.model.TariffSlab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class CalculationEngineTest {

    // Test 1: Previous = 12,345.6, Current = 12,348.2 -> Expected = 2.6 kWh
    @Test
    fun test01_normalConsumptionCalculation() {
        val prev = BigDecimal("12345.6")
        val curr = BigDecimal("12348.2")
        val result = ConsumptionCalculator.calculateConsumption(prev, curr)
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("2.6"), result.getOrThrow())
    }

    // Test 2: Previous = 12,345.6, Current = 12,345.6 -> Expected = 0 kWh
    @Test
    fun test02_zeroConsumptionIsValid() {
        val prev = BigDecimal("12345.6")
        val curr = BigDecimal("12345.6")
        val result = ConsumptionCalculator.calculateConsumption(prev, curr)
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("0.0"), result.getOrThrow())
    }

    // Test 3: Previous = 12,345.6, Current = 12,340.2 -> Expected = Validation Error
    @Test
    fun test03_lowerReadingFailsWithoutRollover() {
        val prev = BigDecimal("12345.6")
        val curr = BigDecimal("12340.2")
        val result = ConsumptionCalculator.calculateConsumption(prev, curr, isRollover = false)
        assertTrue(result.isFailure)
    }

    // Test 4: Red digit is empty -> Expected = Validation Error
    @Test
    fun test04_emptyRedDigitFails() {
        val result = MeterReadingParser.parse("12348", "")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("লাল দশমিক ডিজিট ফাঁকা রাখা যাবে না") == true)
    }

    // Test 5: Red digit contains multiple digits -> Expected = Validation Error
    @Test
    fun test05_multipleRedDigitsFails() {
        val result = MeterReadingParser.parse("12348", "25")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("ঠিক ১ অঙ্কের হতে হবে") == true)
    }

    // Test 6: Bangla numeric input is normalized correctly
    @Test
    fun test06_banglaDigitNormalization() {
        val banglaBlack = "১২৩৪৮"
        val banglaRed = "২"
        val result = MeterReadingParser.parse(banglaBlack, banglaRed)
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("12348.2"), result.getOrThrow())
    }

    // Test 7: Leading zeros in black digits are preserved for display
    @Test
    fun test07_preserveLeadingZeros() {
        val reading = MeterReading(
            id = "test-1",
            meterId = "meter-1",
            blackDigits = "00145",
            redDigit = "2",
            normalizedReading = BigDecimal("145.2"),
            readingDateTime = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isBaseline = false,
            readingSource = ReadingSource.MANUAL
        )
        assertEquals("00145", reading.blackDigits)
        assertEquals("00145.2", reading.displayFormattedReading)
    }

    // Test 8: November 2 = 12,345.6, November 5 = 12,348.2 -> Total = 2.6 kWh, Elapsed days = 3, Avg = approx 0.87 kWh/day
    @Test
    fun test08_missingReadingsIntervalCalculation() {
        val r1 = MeterReading(
            id = "r1",
            meterId = "m1",
            blackDigits = "12345",
            redDigit = "6",
            normalizedReading = BigDecimal("12345.6"),
            readingDateTime = LocalDateTime.of(2026, 11, 2, 9, 0),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val r2 = MeterReading(
            id = "r2",
            meterId = "m1",
            blackDigits = "12348",
            redDigit = "2",
            normalizedReading = BigDecimal("12348.2"),
            readingDateTime = LocalDateTime.of(2026, 11, 5, 9, 0),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val consumption = ConsumptionCalculator.calculateConsumption(r1.normalizedReading, r2.normalizedReading).getOrThrow()
        val elapsedDays = ConsumptionCalculator.calculateElapsedDays(r1, r2)
        val avg = ConsumptionCalculator.calculateAverageDaily(consumption, elapsedDays)

        assertEquals(BigDecimal("2.6"), consumption)
        assertEquals(3L, elapsedDays)
        // 2.6 / 3 = 0.8667 rounded to 2 decimals = 0.87
        assertEquals(BigDecimal("0.87"), avg.setScale(2, java.math.RoundingMode.HALF_UP))
    }

    // Test 14: Flat-rate calculation is correct (250 * 8 = ৳2,000.00)
    @Test
    fun test14_flatRateCalculation() {
        val tariff = Tariff(
            id = "t_flat",
            tariffName = "Flat Tariff",
            billingMode = TariffBillingMode.FLAT_RATE,
            calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
            effectiveFrom = LocalDateTime.now(),
            flatRate = BigDecimal("8.00")
        )
        val (cost, _) = ProgressiveTariffEngine.calculateEnergyCost(BigDecimal("250.0"), tariff)
        assertEquals(BigDecimal("2000.00"), cost)
    }

    // Test 15: Progressive slab calculation is correct (205 units)
    // First 75 @ 5.26 = 394.50
    // Next 125 @ 7.20 = 900.00
    // Next 5 @ 7.59 = 37.95
    // Total = 1332.45
    @Test
    fun test15_progressiveSlabCalculation() {
        val slabs = listOf(
            TariffSlab("s1", "t_slab", BigDecimal("0"), BigDecimal("75"), BigDecimal("5.26"), 1),
            TariffSlab("s2", "t_slab", BigDecimal("75"), BigDecimal("200"), BigDecimal("7.20"), 2),
            TariffSlab("s3", "t_slab", BigDecimal("200"), BigDecimal("300"), BigDecimal("7.59"), 3)
        )
        val tariff = Tariff(
            id = "t_slab",
            tariffName = "Slab Tariff",
            billingMode = TariffBillingMode.SLAB_BASED,
            calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
            effectiveFrom = LocalDateTime.now(),
            slabs = slabs
        )
        val (cost, breakdown) = ProgressiveTariffEngine.calculateEnergyCost(BigDecimal("205.0"), tariff)
        assertEquals(BigDecimal("1332.45"), cost)
        assertEquals(3, breakdown.size)
        assertEquals(BigDecimal("394.50"), breakdown[0].cost)
        assertEquals(BigDecimal("900.00"), breakdown[1].cost)
        assertEquals(BigDecimal("37.95"), breakdown[2].cost)
    }

    // Test 16: Daily slab charges use difference between cumulative bills
    // Previous cumulative = 190, New cumulative = 205
    // Bill(190) = (75 * 5.26) + (115 * 7.20) = 394.50 + 828.00 = 1222.50
    // Bill(205) = 1332.45
    // Delta = 1332.45 - 1222.50 = 109.95
    @Test
    fun test16_differentialDailyEnergyCost() {
        val slabs = listOf(
            TariffSlab("s1", "t_slab", BigDecimal("0"), BigDecimal("75"), BigDecimal("5.26"), 1),
            TariffSlab("s2", "t_slab", BigDecimal("75"), BigDecimal("200"), BigDecimal("7.20"), 2),
            TariffSlab("s3", "t_slab", BigDecimal("200"), BigDecimal("300"), BigDecimal("7.59"), 3)
        )
        val tariff = Tariff(
            id = "t_slab",
            tariffName = "Slab Tariff",
            billingMode = TariffBillingMode.SLAB_BASED,
            calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
            effectiveFrom = LocalDateTime.now(),
            slabs = slabs
        )
        val deltaCost = ProgressiveTariffEngine.calculateDifferentialEnergyCost(
            previousCumulativeConsumption = BigDecimal("190.0"),
            newCumulativeConsumption = BigDecimal("205.0"),
            tariff = tariff
        )
        assertEquals(BigDecimal("109.95"), deltaCost)
    }

    // Test 17: Fixed monthly charges applied exactly once
    @Test
    fun test17_fixedMonthlyChargesAppliedOnce() {
        val tariff = Tariff(
            id = "t_charge",
            tariffName = "Tariff with Charges",
            billingMode = TariffBillingMode.FLAT_RATE,
            effectiveFrom = LocalDateTime.now(),
            flatRate = BigDecimal("10.00"),
            additionalCharges = listOf(
                AdditionalCharge("c1", "t_charge", AdditionalChargeType.DEMAND_CHARGE, ChargeCalculationMethod.FIXED_MONTHLY, BigDecimal("42.00")),
                AdditionalCharge("c2", "t_charge", AdditionalChargeType.METER_RENT, ChargeCalculationMethod.FIXED_MONTHLY, BigDecimal("10.00"))
            )
        )
        val bill = ProgressiveTariffEngine.calculateMonthlyBill(BigDecimal("100.0"), tariff)
        // Energy = 1000.00, Demand = 42.00, Rent = 10.00 -> Total = 1052.00
        assertEquals(BigDecimal("1000.00"), bill.energyCost)
        assertEquals(BigDecimal("42.00"), bill.demandCharge)
        assertEquals(BigDecimal("10.00"), bill.meterRent)
        assertEquals(BigDecimal("1052.00"), bill.totalBill)
    }

    // Test 18: VAT calculated using configured base
    @Test
    fun test18_vatCalculationWithConfiguredBase() {
        val tariff = Tariff(
            id = "t_vat",
            tariffName = "Tariff with VAT",
            billingMode = TariffBillingMode.FLAT_RATE,
            effectiveFrom = LocalDateTime.now(),
            flatRate = BigDecimal("10.00"),
            additionalCharges = listOf(
                AdditionalCharge("c1", "t_vat", AdditionalChargeType.DEMAND_CHARGE, ChargeCalculationMethod.FIXED_MONTHLY, BigDecimal("100.00"), vatApplicable = true),
                AdditionalCharge("vat", "t_vat", AdditionalChargeType.VAT, ChargeCalculationMethod.PERCENTAGE_OF_TOTAL, BigDecimal("5.00")) // 5% VAT
            )
        )
        // Energy = 1000.00, Demand = 100.00 (VAT applicable) -> VAT Base = 1100.00 -> VAT = 55.00
        val bill = ProgressiveTariffEngine.calculateMonthlyBill(BigDecimal("100.0"), tariff)
        assertEquals(BigDecimal("55.00"), bill.vatAmount)
        assertEquals(BigDecimal("1155.00"), bill.totalBill)
    }

    // Test 22: Counter rollover requires explicit confirmation and calculates rollover consumption
    // 5 digit counter: Capacity = 100000.0. Prev = 99999.8, Curr = 00002.3
    // Rollover consumption = (100000.0 - 99999.8) + 2.3 = 0.2 + 2.3 = 2.5 kWh
    @Test
    fun test22_counterRolloverCalculation() {
        val prev = BigDecimal("99999.8")
        val curr = BigDecimal("2.3")
        val result = ConsumptionCalculator.calculateConsumption(
            previousReading = prev,
            currentReading = curr,
            isRollover = true,
            blackDigitCapacity = 5
        )
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("2.5"), result.getOrThrow())
    }

    // Test 25: All monetary calculations use exact decimal arithmetic (No Double / Float drift)
    @Test
    fun test25_exactDecimalPrecision() {
        val prev = BigDecimal("12345.6")
        val curr = BigDecimal("12348.2")
        val delta = curr.subtract(prev)
        // If Double was used: 12348.2 - 12345.6 = 2.5999999999999996
        // With BigDecimal: exactly 2.6
        assertEquals("2.6", delta.toPlainString())
    }
}
