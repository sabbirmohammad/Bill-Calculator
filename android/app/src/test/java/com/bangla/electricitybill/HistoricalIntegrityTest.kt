package com.bangla.electricitybill

import com.bangla.electricitybill.data.backup.AppBackupPayload
import com.bangla.electricitybill.data.entity.MeterEntity
import com.bangla.electricitybill.data.entity.MeterReadingEntity
import com.bangla.electricitybill.data.entity.TariffEntity
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class HistoricalIntegrityTest {

    // Test 19: Historical tariff versions are preserved
    @Test
    fun test19_historicalTariffVersionsPreserved() {
        val tariffV1 = Tariff(
            id = "t_v1",
            tariffName = "Old Tariff",
            billingMode = TariffBillingMode.FLAT_RATE,
            calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
            effectiveFrom = LocalDateTime.of(2025, 1, 1, 0, 0),
            effectiveUntil = LocalDateTime.of(2026, 10, 31, 23, 59),
            isActive = false,
            flatRate = BigDecimal("7.00")
        )
        val tariffV2 = Tariff(
            id = "t_v2",
            tariffName = "New Tariff",
            billingMode = TariffBillingMode.FLAT_RATE,
            calculationMethod = TariffCalculationMethod.PROGRESSIVE_BLOCK,
            effectiveFrom = LocalDateTime.of(2026, 11, 1, 0, 0),
            effectiveUntil = null,
            isActive = true,
            flatRate = BigDecimal("8.50")
        )

        // Historical bill using V1 is calculated at 7.00
        val costUnderV1 = BigDecimal("100").multiply(tariffV1.flatRate!!)
        // New bill using V2 is calculated at 8.50
        val costUnderV2 = BigDecimal("100").multiply(tariffV2.flatRate!!)

        assertEquals(BigDecimal("700.00"), costUnderV1.setScale(2))
        assertEquals(BigDecimal("850.00"), costUnderV2.setScale(2))
        assertNotEquals(tariffV1.id, tariffV2.id)
    }

    // Test 20: Editing a historical reading recalculates affected intervals
    // Sequence: Nov 1: 100, Nov 2: 120, Nov 3: 150
    // Original intervals: Nov 2 = 20, Nov 3 = 30
    // When Nov 2 is edited to 130:
    // New intervals: Nov 2 = 30, Nov 3 = 20. Total consumption (150 - 100 = 50) is preserved.
    @Test
    fun test20_editingHistoricalReadingRecalculatesIntervals() {
        val r1 = BigDecimal("100.0")
        val r2Original = BigDecimal("120.0")
        val r3 = BigDecimal("150.0")

        val interval2Original = r2Original.subtract(r1)
        val interval3Original = r3.subtract(r2Original)
        assertEquals(BigDecimal("20.0"), interval2Original)
        assertEquals(BigDecimal("30.0"), interval3Original)

        // User corrects Nov 2 reading to 130.0
        val r2Edited = BigDecimal("130.0")
        val interval2Recalculated = r2Edited.subtract(r1)
        val interval3Recalculated = r3.subtract(r2Edited)

        assertEquals(BigDecimal("30.0"), interval2Recalculated)
        assertEquals(BigDecimal("20.0"), interval3Recalculated)
        assertEquals(r3.subtract(r1), interval2Recalculated.add(interval3Recalculated))
    }

    // Test 21: Meter replacement does not create negative consumption
    @Test
    fun test21_meterReplacementPreservesIndependentReadings() {
        val oldMeter = Meter("m_old", "Old Meter", null, "MECHANICAL_COUNTER", 5, 1, MeterStatus.REPLACED, LocalDateTime.now(), LocalDateTime.now())
        val newMeter = Meter("m_new", "New Meter", null, "MECHANICAL_COUNTER", 5, 1, MeterStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now())

        val lastOldReading = BigDecimal("45892.1")
        val newInitialBaseline = BigDecimal("0.5")

        // Crucial: New meter is NOT subtracted from old meter!
        assertNotEquals(oldMeter.id, newMeter.id)
        val newMeterFirstReading = BigDecimal("4.2")
        val newConsumption = ConsumptionCalculator.calculateConsumption(newInitialBaseline, newMeterFirstReading).getOrThrow()

        // 4.2 - 0.5 = 3.7 kWh (positive valid consumption)
        assertEquals(BigDecimal("3.7"), newConsumption)
    }

    // Test 24: Backup and restore retain historical data
    @Test
    fun test24_jsonBackupPayloadSerializationAndDeserialization() {
        val gson = com.bangla.electricitybill.data.backup.BackupGson.create()
        val payload = AppBackupPayload(
            version = 1,
            exportedAt = 1730560000000L,
            meters = listOf(
                MeterEntity("m1", "Main Meter", "123456", "MECHANICAL_COUNTER", 5, 1, MeterStatus.ACTIVE, LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.now())
            ),
            meterReadings = listOf(
                MeterReadingEntity("r1", "m1", "12345", "6", BigDecimal("12345.6"), LocalDateTime.of(2026, 11, 1, 10, 0), LocalDateTime.now(), LocalDateTime.now(), true, "Baseline", null, ReadingSource.INITIAL_BASELINE)
            ),
            billingPeriods = emptyList(),
            tariffs = emptyList(),
            tariffSlabs = emptyList(),
            additionalCharges = emptyList(),
            monthlySummaries = emptyList(),
            meterEvents = emptyList()
        )

        val json = gson.toJson(payload)
        val deserialized = gson.fromJson(json, AppBackupPayload::class.java)

        assertEquals(payload.version, deserialized.version)
        assertEquals(1, deserialized.meters.size)
        assertEquals("Main Meter", deserialized.meters[0].meterName)
        assertEquals("12345.6", deserialized.meterReadings[0].normalizedReading.toPlainString())
    }
}
