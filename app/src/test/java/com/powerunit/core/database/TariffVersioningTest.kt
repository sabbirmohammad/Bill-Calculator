package com.powerunit.core.database

import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.entity.TariffVersionEntity
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.data.repository.TariffRepositoryImpl
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.model.TariffVersion
import com.powerunit.domain.usecase.CalculateBillUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TariffVersioningTest {

    private val calculateBillUseCase = CalculateBillUseCase()

    @Test
    fun slabSerialization_roundTrip_preservesExactValuesAndOpenEnded() {
        val originalSlabs = listOf(
            TariffSlab(fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.85")),
            TariffSlab(fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
            TariffSlab(fromUnit = BigDecimal("201"), toUnit = null, ratePerUnit = BigDecimal("8.50"))
        )

        val jsonString = TariffVersionEntity.serializeSlabs(originalSlabs)
        assertTrue(jsonString.contains("4.85"))
        assertTrue(jsonString.contains("75"))

        val deserialized = TariffVersionEntity.deserializeSlabs(jsonString)
        assertEquals(3, deserialized.size)
        assertEquals(BigDecimal("0"), deserialized[0].fromUnit)
        assertEquals(BigDecimal("75"), deserialized[0].toUnit)
        assertEquals(BigDecimal("4.85"), deserialized[0].ratePerUnit)

        assertEquals(BigDecimal("201"), deserialized[2].fromUnit)
        assertEquals(null, deserialized[2].toUnit)
        assertEquals(BigDecimal("8.50"), deserialized[2].ratePerUnit)
    }

    @Test
    fun cycleStrings_lexicographicalOrder_matchesChronologicalOrder() {
        val cycle202511 = "2025-11"
        val cycle202512 = "2025-12"
        val cycle202601 = "2026-01"
        val cycle202611 = "2026-11"

        assertTrue(cycle202511 < cycle202512)
        assertTrue(cycle202512 < cycle202601)
        assertTrue(cycle202601 < cycle202611)

        val parsedInstant = DateUtils.cycleStringToInstant("2026-11")
        assertNotNull(parsedInstant)
        assertEquals("2026-11", DateUtils.instantToCycleString(parsedInstant))
        assertEquals("Nov 2026", DateUtils.formatCycleMonth("2026-11"))
    }

    @Test
    fun recalculation_idempotency_producesIdenticalBillBreakdown() {
        val slabs = listOf(
            TariffSlab(fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.85")),
            TariffSlab(fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
            TariffSlab(fromUnit = BigDecimal("201"), toUnit = null, ratePerUnit = BigDecimal("10.00"))
        )
        val version = TariffVersion(
            planId = 1L,
            effectiveFromCycle = "2026-01",
            slabs = slabs,
            fixedCharge = BigDecimal("10.00"),
            demandCharge = BigDecimal("35.00"),
            vatPercent = BigDecimal("5.0"),
            rebatePercent = BigDecimal.ZERO,
            otherCharges = BigDecimal.ZERO
        )
        val tariff = version.toTariff()

        val units = BigDecimal("150.0")
        val billRun1 = calculateBillUseCase(units, tariff)
        val billRun2 = calculateBillUseCase(units, tariff)
        val billRun3 = calculateBillUseCase(units, tariff)

        assertEquals(billRun1.totalBill, billRun2.totalBill)
        assertEquals(billRun2.totalBill, billRun3.totalBill)
        assertEquals(billRun1.energyCharge, billRun3.energyCharge)
        assertEquals(billRun1.vatAmount, billRun3.vatAmount)
    }

    @Test
    fun seedDefaultsIfNeeded_whenAlreadySeeded_doesNotOverwrite() = runTest {
        val mockDb = mockk<PowerUnitDatabase>(relaxed = true)
        val mockTariffDao = mockk<com.powerunit.core.database.dao.TariffDao>(relaxed = true)
        val mockVersionDao = mockk<com.powerunit.core.database.dao.TariffVersionDao>(relaxed = true)
        val mockPrefs = mockk<SettingsPreferences>(relaxed = true)

        coEvery { mockPrefs.isTariffSeeded() } returns true
        coEvery { mockVersionDao.getCount() } returns 1

        val repository = TariffRepositoryImpl(
            database = mockDb,
            tariffDao = mockTariffDao,
            tariffVersionDao = mockVersionDao,
            settingsPreferences = mockPrefs
        )

        repository.seedDefaultsIfNeeded()

        // Verify that insertVersion was NEVER called because already seeded
        coVerify(exactly = 0) { mockVersionDao.insertVersion(any()) }
    }

    @Test
    fun seedDefaultsIfNeeded_whenEmptyAndNotSeeded_seedsBaselineVersion() = runTest {
        val mockDb = mockk<PowerUnitDatabase>(relaxed = true)
        val mockTariffDao = mockk<com.powerunit.core.database.dao.TariffDao>(relaxed = true)
        val mockVersionDao = mockk<com.powerunit.core.database.dao.TariffVersionDao>(relaxed = true)
        val mockPrefs = mockk<SettingsPreferences>(relaxed = true)

        coEvery { mockPrefs.isTariffSeeded() } returns false
        coEvery { mockVersionDao.getCount() } returns 0
        coEvery { mockVersionDao.insertVersion(any()) } returns 1L
        coEvery { mockPrefs.setTariffSeeded(true) } returns Unit

        val repository = TariffRepositoryImpl(
            database = mockDb,
            tariffDao = mockTariffDao,
            tariffVersionDao = mockVersionDao,
            settingsPreferences = mockPrefs
        )

        repository.seedDefaultsIfNeeded()

        // Verify that insertVersion WAS called with 1970-01 baseline
        coVerify(exactly = 1) {
            mockVersionDao.insertVersion(match { it.effectiveFromCycle == "1970-01" })
        }
        coVerify(exactly = 1) { mockPrefs.setTariffSeeded(true) }
    }
}
