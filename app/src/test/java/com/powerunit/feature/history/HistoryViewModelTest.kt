package com.powerunit.feature.history

import androidx.lifecycle.SavedStateHandle
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.export.ExportManager
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
    private val calculateBillUseCase = CalculateBillUseCase()
    private val exportManager = mockk<ExportManager>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)

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

    private val readingsList = listOf(
        Reading(
            id = 2L,
            meterId = 1L,
            readingValue = BigDecimal("120.0"),
            recordedAt = Instant.parse("2026-03-05T10:00:00Z"),
            note = "March reading",
            isEstimated = false,
            isMeterReset = false,
            createdAt = Instant.parse("2026-03-05T10:00:00Z"),
            updatedAt = Instant.parse("2026-03-05T10:00:00Z")
        ),
        Reading(
            id = 1L,
            meterId = 1L,
            readingValue = BigDecimal("100.0"),
            recordedAt = Instant.parse("2026-03-01T10:00:00Z"),
            note = "Start reading",
            isEstimated = false,
            isMeterReset = false,
            createdAt = Instant.parse("2026-03-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-03-01T10:00:00Z")
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { meterRepository.getActiveMeterSync() } returns sampleMeter
        coEvery { readingRepository.getReadings(1L) } returns flowOf(readingsList)
        coEvery { tariffRepository.getActiveTariff() } returns flowOf(sampleTariff)
        every { settingsPreferences.useBengaliDigits } returns flowOf(false)
        every { settingsPreferences.appLanguage } returns flowOf("en")
        every { settingsPreferences.hideAmounts } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): HistoryViewModel {
        return HistoryViewModel(
            savedStateHandle = savedStateHandle,
            readingRepository = readingRepository,
            meterRepository = meterRepository,
            tariffRepository = tariffRepository,
            calculateBillUseCase = calculateBillUseCase,
            exportManager = exportManager,
            settingsPreferences = settingsPreferences
        )
    }

    @Test
    fun loadReadings_defaultsToCurrentYearAndMonth() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(viewModel.defaultYear, state.selectedYear)
        assertEquals(viewModel.defaultMonth, state.selectedMonth)
        assertEquals(12, state.monthChips.size)
        assertTrue(state.availableYears.contains(2026))
    }

    @Test
    fun onSelectMonth_filtersReadingsForSelectedMonth() = runTest(testDispatcher) {
        val savedState = SavedStateHandle(mapOf(
            HistoryViewModel.KEY_SELECTED_YEAR to 2026,
            HistoryViewModel.KEY_SELECTED_MONTH to 3
        ))
        val viewModel = createViewModel(savedState)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.selectedMonth)
        assertEquals(2, state.items.size)
        assertEquals(1, state.monthGroups.size)
        assertNotNull(state.monthSummary)
        assertEquals(BigDecimal("20.0"), state.monthSummary?.totalUnits)
        assertEquals(2, state.monthSummary?.readingsCount)
        assertTrue(state.monthSummary!!.totalCost > BigDecimal.ZERO)
        assertTrue(state.monthChips[2].hasData) // Month 3 has data
    }

    @Test
    fun onSelectMonth_emptyMonth_hasNullSummaryAndEmptyItems() = runTest(testDispatcher) {
        val savedState = SavedStateHandle(mapOf(
            HistoryViewModel.KEY_SELECTED_YEAR to 2026,
            HistoryViewModel.KEY_SELECTED_MONTH to 1 // January has no readings in readingsList
        ))
        val viewModel = createViewModel(savedState)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedMonth)
        assertTrue(state.items.isEmpty())
        assertNull(state.monthSummary)
        assertFalse(state.monthChips[0].hasData) // Month 1 has no data
    }

    @Test
    fun onSelectYear_updatesStateCorrectly() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSelectYear(2025)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2025, state.selectedYear)
    }

    @Test
    fun deleteReadingAndUndo_callsRepositoryAppropriately() = runTest(testDispatcher) {
        val savedState = SavedStateHandle(mapOf(
            HistoryViewModel.KEY_SELECTED_YEAR to 2026,
            HistoryViewModel.KEY_SELECTED_MONTH to 3
        ))
        val viewModel = createViewModel(savedState)
        testDispatcher.scheduler.advanceUntilIdle()

        val itemToDelete = viewModel.uiState.value.items[0]
        viewModel.deleteReading(itemToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { readingRepository.deleteReading(itemToDelete.id) }

        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { readingRepository.saveReading(itemToDelete.rawReading) }
    }

    @Test
    fun onSelectItemForDetail_updatesSelectedDetailItem() = runTest(testDispatcher) {
        val savedState = SavedStateHandle(mapOf(
            HistoryViewModel.KEY_SELECTED_YEAR to 2026,
            HistoryViewModel.KEY_SELECTED_MONTH to 3
        ))
        val viewModel = createViewModel(savedState)
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.items[0]
        viewModel.onSelectItemForDetail(item)

        assertEquals(item, viewModel.uiState.value.selectedDetailItem)
    }

    @Test
    fun hideAmounts_updatesState() = runTest(testDispatcher) {
        every { settingsPreferences.hideAmounts } returns flowOf(true)
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hideAmounts)
    }
}
