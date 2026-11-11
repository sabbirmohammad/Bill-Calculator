package com.powerunit.feature.bill

import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class BillViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)

    private val calculateDailyUsageUseCase = com.powerunit.domain.usecase.CalculateDailyUsageUseCase()
    private val calculateCycleUsageUseCase = CalculateCycleUsageUseCase()
    private val calculateBillUseCase = CalculateBillUseCase()

    private val sampleMeter = Meter(
        id = 1L,
        name = "Home Meter",
        type = MeterType.POSTPAID,
        startReading = BigDecimal("100.0"),
        multiplierFactor = BigDecimal.ONE,
        isActive = true,
        createdAt = DateUtils.currentInstant()
    )

    private val sampleTariff = Tariff(
        id = 1L,
        name = "DESCO Postpaid",
        effectiveFrom = DateUtils.currentInstant(),
        vatPercent = BigDecimal("5.0"),
        serviceChargeFlat = BigDecimal("10.00"),
        demandChargeFlat = BigDecimal("35.00"),
        slabs = listOf(
            TariffSlab(1L, 1L, BigDecimal("0"), BigDecimal("75"), BigDecimal("4.85")),
            TariffSlab(2L, 1L, BigDecimal("76"), BigDecimal("200"), BigDecimal("6.63"))
        ),
        isActive = true
    )

    private val now = DateUtils.currentInstant()
    private val yesterday = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 86400000L)

    private val sampleReadings = listOf(
        Reading(
            id = 1L,
            meterId = 1L,
            readingValue = BigDecimal("100.0"),
            recordedAt = yesterday,
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = yesterday,
            updatedAt = yesterday
        ),
        Reading(
            id = 2L,
            meterId = 1L,
            readingValue = BigDecimal("150.0"),
            recordedAt = now,
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = now,
            updatedAt = now
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { meterRepository.getActiveMeterSync() } returns sampleMeter
        coEvery { readingRepository.getReadings(1L) } returns flowOf(sampleReadings)
        coEvery { tariffRepository.getActiveTariff() } returns flowOf(sampleTariff)
        coEvery { settingsPreferences.billingCycleDay } returns flowOf(1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadBillData_computesActualBillAndInitialSimulatedBill() = runTest(testDispatcher) {
        val viewModel = BillViewModel(
            meterRepository = meterRepository,
            readingRepository = readingRepository,
            tariffRepository = tariffRepository,
            settingsPreferences = settingsPreferences,
            calculateDailyUsageUseCase = calculateDailyUsageUseCase,
            calculateCycleUsageUseCase = calculateCycleUsageUseCase,
            calculateBillUseCase = calculateBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.actualBill)
        assertNotNull(state.simulatedBill)
        assertEquals(BigDecimal("50.0"), state.actualCycleUsage?.totalUnitsSoFar)
    }

    @Test
    fun onSimulatedUnitsChange_updatesSimulatedBillAndDelta() = runTest(testDispatcher) {
        val viewModel = BillViewModel(
            meterRepository = meterRepository,
            readingRepository = readingRepository,
            tariffRepository = tariffRepository,
            settingsPreferences = settingsPreferences,
            calculateDailyUsageUseCase = calculateDailyUsageUseCase,
            calculateCycleUsageUseCase = calculateCycleUsageUseCase,
            calculateBillUseCase = calculateBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSimulatedUnitsChange(BigDecimal("200"))

        val state = viewModel.uiState.value
        assertEquals(BigDecimal("200"), state.simulatedUnits)
        assertNotNull(state.simulatedBill)
        assertTrue((state.billDeltaVsActual ?: BigDecimal.ZERO) > BigDecimal.ZERO)
    }

    @Test
    fun togglePaidStatus_updatesPaidFlag() = runTest(testDispatcher) {
        val viewModel = BillViewModel(
            meterRepository = meterRepository,
            readingRepository = readingRepository,
            tariffRepository = tariffRepository,
            settingsPreferences = settingsPreferences,
            calculateDailyUsageUseCase = calculateDailyUsageUseCase,
            calculateCycleUsageUseCase = calculateCycleUsageUseCase,
            calculateBillUseCase = calculateBillUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPastCyclePaid)
        viewModel.togglePaidStatus()
        assertTrue(viewModel.uiState.value.isPastCyclePaid)
    }
}
