package com.powerunit.feature.analytics

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
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import com.powerunit.domain.usecase.CompareWithPreviousUseCase
import com.powerunit.domain.usecase.DetectAnomaliesUseCase
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
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)

    private val calculateDailyUsageUseCase = CalculateDailyUsageUseCase()
    private val calculateBillUseCase = CalculateBillUseCase()
    private val compareWithPreviousUseCase = CompareWithPreviousUseCase()
    private val detectAnomaliesUseCase = DetectAnomaliesUseCase()

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
    private val day1 = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 86400000L * 3)
    private val day2 = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 86400000L * 2)
    private val day3 = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() - 86400000L)

    private val sampleReadings = listOf(
        Reading(
            id = 1L,
            meterId = 1L,
            readingValue = BigDecimal("100.0"),
            recordedAt = day1,
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = day1,
            updatedAt = day1
        ),
        Reading(
            id = 2L,
            meterId = 1L,
            readingValue = BigDecimal("115.0"),
            recordedAt = day2,
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = day2,
            updatedAt = day2
        ),
        Reading(
            id = 3L,
            meterId = 1L,
            readingValue = BigDecimal("140.0"),
            recordedAt = day3,
            note = null,
            isEstimated = false,
            isMeterReset = false,
            createdAt = day3,
            updatedAt = day3
        ),
        Reading(
            id = 4L,
            meterId = 1L,
            readingValue = BigDecimal("160.0"),
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
        coEvery { settingsPreferences.useBengaliDigits } returns flowOf(false)
        coEvery { settingsPreferences.appLanguage } returns flowOf("en")
        coEvery { settingsPreferences.hideAmounts } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AnalyticsViewModel {
        return AnalyticsViewModel(
            meterRepository = meterRepository,
            readingRepository = readingRepository,
            tariffRepository = tariffRepository,
            settingsPreferences = settingsPreferences,
            calculateDailyUsageUseCase = calculateDailyUsageUseCase,
            calculateBillUseCase = calculateBillUseCase,
            compareWithPreviousUseCase = compareWithPreviousUseCase,
            detectAnomaliesUseCase = detectAnomaliesUseCase
        )
    }

    @Test
    fun loadData_computesChartAndSummaryForDayPeriod() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(AnalyticsPeriod.DAY, state.selectedPeriod)
        assertEquals(3, state.chartData.size) // 4 readings = 3 delta days
        assertEquals(BigDecimal("60.0"), state.totalUnits) // 15 + 25 + 20 = 60
        assertTrue(state.totalCost > BigDecimal.ZERO)
        assertNotNull(state.peakUsage)
        assertEquals(BigDecimal("25.0"), state.peakUsage?.units)
        assertEquals(7, state.weekdayPattern.size) // Mon..Sun
    }

    @Test
    fun selectPeriod_week_aggregatesIntoWeeklyBars() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(AnalyticsPeriod.WEEK)
        val state = viewModel.uiState.value

        assertEquals(AnalyticsPeriod.WEEK, state.selectedPeriod)
        assertTrue(state.chartData.isNotEmpty())
        assertEquals(BigDecimal("60.0"), state.totalUnits)
    }

    @Test
    fun selectPeriod_month_aggregatesIntoMonthlyBars() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPeriod(AnalyticsPeriod.MONTH)
        val state = viewModel.uiState.value

        assertEquals(AnalyticsPeriod.MONTH, state.selectedPeriod)
        assertTrue(state.chartData.isNotEmpty())
    }

    @Test
    fun toggleTableView_togglesTableMode() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTableView)
        viewModel.toggleTableView()
        assertTrue(viewModel.uiState.value.isTableView)
        viewModel.toggleTableView()
        assertFalse(viewModel.uiState.value.isTableView)
    }

    @Test
    fun selectBar_updatesSelectedBarIndex() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectBar(1)
        assertEquals(1, viewModel.uiState.value.selectedBarIndex)
        viewModel.selectBar(null)
        assertEquals(null, viewModel.uiState.value.selectedBarIndex)
    }
}
