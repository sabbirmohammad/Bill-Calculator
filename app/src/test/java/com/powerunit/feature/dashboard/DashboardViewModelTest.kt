package com.powerunit.feature.dashboard

import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.BudgetRepository
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import com.powerunit.domain.usecase.ForecastMonthEndUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val budgetRepository = mockk<BudgetRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)

    private val calculateDailyUsageUseCase = CalculateDailyUsageUseCase()
    private val calculateCycleUsageUseCase = CalculateCycleUsageUseCase()
    private val calculateBillUseCase = CalculateBillUseCase()
    private val forecastMonthEndUseCase = ForecastMonthEndUseCase(calculateBillUseCase)

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

    private val sampleReadings = listOf(
        Reading(
            id = 1L,
            meterId = 1L,
            readingValue = BigDecimal("100.0"),
            recordedAt = DateUtils.currentInstant(),
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = DateUtils.currentInstant(),
            updatedAt = DateUtils.currentInstant()
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { meterRepository.getActiveMeterSync() } returns sampleMeter
        coEvery { readingRepository.getReadings(1L) } returns flowOf(sampleReadings)
        coEvery { tariffRepository.getActiveTariff() } returns flowOf(sampleTariff)
        coEvery { settingsPreferences.billingCycleDay } returns flowOf(1)
        coEvery { settingsPreferences.hideAmounts } returns flowOf(false)
        coEvery { settingsPreferences.useBengaliDigits } returns flowOf(false)
        coEvery { settingsPreferences.appLanguage } returns flowOf("en")
        coEvery { budgetRepository.getLatestBudget() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            meterRepository = meterRepository,
            readingRepository = readingRepository,
            tariffRepository = tariffRepository,
            budgetRepository = budgetRepository,
            settingsPreferences = settingsPreferences,
            calculateDailyUsageUseCase = calculateDailyUsageUseCase,
            calculateCycleUsageUseCase = calculateCycleUsageUseCase,
            calculateBillUseCase = calculateBillUseCase,
            forecastMonthEndUseCase = forecastMonthEndUseCase
        )
    }

    @Test
    fun loadDashboardData_computesStateCorrectly() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Home Meter", state.activeMeter?.name)
        assertNotNull(state.cycleUsage)
        assertNotNull(state.billBreakdown)
        assertNotNull(state.budget)
        assertTrue(state.hasInitialReading)
        assertFalse(state.showInitialReadingDialog)
    }

    @Test
    fun whenNoReadings_showsInitialReadingDialog() = runTest(testDispatcher) {
        coEvery { readingRepository.getReadings(1L) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasInitialReading)
        assertTrue(state.showInitialReadingDialog)
    }

    @Test
    fun onDismissInitialReadingDialog_hidesDialog() = runTest(testDispatcher) {
        coEvery { readingRepository.getReadings(1L) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showInitialReadingDialog)

        viewModel.onDismissInitialReadingDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showInitialReadingDialog)
        assertFalse(viewModel.uiState.value.hasInitialReading)

        viewModel.onShowInitialReadingDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showInitialReadingDialog)
    }

    @Test
    fun saveInitialReading_savesReading_andUpdatesMeter() = runTest(testDispatcher) {
        coEvery { readingRepository.getReadings(1L) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val customInstant = DateUtils.toInstantAtDhaka(2026, 11, 1, 8, 0)
        viewModel.saveInitialReading(
            readingValue = BigDecimal("1250.0"),
            recordedAt = customInstant,
            note = "First reading"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            readingRepository.saveReading(match {
                it.readingValue == BigDecimal("1250.0") &&
                    it.recordedAt == customInstant &&
                    it.note == "First reading"
            })
            meterRepository.updateMeter(match {
                it.startReading == BigDecimal("1250.0")
            })
            settingsPreferences.setOnboardingCompleted(true)
        }
        assertFalse(viewModel.uiState.value.showInitialReadingDialog)
    }

    @Test
    fun recentReadings_takesUpToThreeReadings_withAccurateDeltaAndTariffCalculation() = runTest(testDispatcher) {
        val multipleReadings = listOf(
            Reading(
                id = 1L,
                meterId = 1L,
                readingValue = BigDecimal("100.0"),
                recordedAt = DateUtils.toInstantAtDhaka(2026, 11, 1, 8, 0),
                note = "Baseline",
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.toInstantAtDhaka(2026, 11, 1, 8, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 11, 1, 8, 0)
            ),
            Reading(
                id = 2L,
                meterId = 1L,
                readingValue = BigDecimal("130.0"), // +30.0 units
                recordedAt = DateUtils.toInstantAtDhaka(2026, 11, 5, 8, 0),
                note = "Reading 2",
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.toInstantAtDhaka(2026, 11, 5, 8, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 11, 5, 8, 0)
            ),
            Reading(
                id = 3L,
                meterId = 1L,
                readingValue = BigDecimal("160.0"), // +30.0 units
                recordedAt = DateUtils.toInstantAtDhaka(2026, 11, 10, 8, 0),
                note = "Reading 3",
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.toInstantAtDhaka(2026, 11, 10, 8, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 11, 10, 8, 0)
            ),
            Reading(
                id = 4L,
                meterId = 1L,
                readingValue = BigDecimal("200.0"), // +40.0 units
                recordedAt = DateUtils.toInstantAtDhaka(2026, 11, 15, 8, 0),
                note = "Reading 4",
                isEstimated = false,
                isMeterReset = false,
                createdAt = DateUtils.toInstantAtDhaka(2026, 11, 15, 8, 0),
                updatedAt = DateUtils.toInstantAtDhaka(2026, 11, 15, 8, 0)
            )
        )
        coEvery { readingRepository.getReadings(1L) } returns flowOf(multipleReadings)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val recent = viewModel.uiState.value.recentReadings
        // Only 3 most recent readings should be shown, ordered newest first
        assertEquals(3, recent.size)
        assertEquals(4L, recent[0].id)
        assertEquals(3L, recent[1].id)
        assertEquals(2L, recent[2].id)

        // Reading 4: +40.0 units (200 - 160)
        assertEquals(BigDecimal("40.0"), recent[0].unitsDelta)
        // Reading 3: +30.0 units (160 - 130)
        assertEquals(BigDecimal("30.0"), recent[1].unitsDelta)
        // Reading 2: +30.0 units (130 - 100), calculated using Reading 1 as predecessor!
        assertEquals(BigDecimal("30.0"), recent[2].unitsDelta)
        // Reading 2 should not show 0 or "Initial reading" since its predecessor exists in history
        assertTrue(recent[2].costEstimate > BigDecimal.ZERO)
        assertNotNull(recent[2].slabLabel)
    }

    @Test
    fun deleteReading_delegatesToRepository() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteReading(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { readingRepository.deleteReading(42L) }
    }
}
