package com.powerunit.feature.addreading

import androidx.lifecycle.SavedStateHandle
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.ReadingValidationResult
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.ValidateReadingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AddReadingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private val validateReadingUseCase = ValidateReadingUseCase()
    private val tariffRepository = mockk<TariffRepository>(relaxed = true)
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

    private val sampleLatestReading = Reading(
        id = 10L,
        meterId = 1L,
        readingValue = BigDecimal("120.0"),
        recordedAt = DateUtils.currentInstant(),
        note = null,
        isEstimated = false,
        isMeterReset = false,
        createdAt = DateUtils.currentInstant(),
        updatedAt = DateUtils.currentInstant()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { meterRepository.getActiveMeterSync() } returns sampleMeter
        coEvery { readingRepository.getLatestReadingSync(1L) } returns sampleLatestReading
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): AddReadingViewModel {
        return AddReadingViewModel(
            readingRepository = readingRepository,
            meterRepository = meterRepository,
            validateReadingUseCase = validateReadingUseCase,
            settingsPreferences = settingsPreferences,
            tariffRepository = tariffRepository,
            calculateBillUseCase = calculateBillUseCase,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun loadInitialData_setsMeterAndPreviousReading() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1L, state.meterId)
        assertEquals(BigDecimal("120.0"), state.previousReadingValue)
        assertFalse(state.isInitialReading)
    }

    @Test
    fun onReadingInputChange_calculatesDelta() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReadingInputChange("135.5")

        val state = viewModel.uiState.value
        assertEquals("135.5", state.readingInput)
        assertEquals(BigDecimal("15.5"), state.calculatedDelta)
        assertEquals(ReadingValidationResult.Valid, state.validationResult)
    }

    @Test
    fun onDateChange_updatesRecordedAt() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val customInstant = DateUtils.toInstantAtDhaka(2026, 11, 1, 10, 30)
        viewModel.onDateChange(customInstant)

        val state = viewModel.uiState.value
        assertEquals(customInstant, state.recordedAt)
    }

    @Test
    fun initialReading_detectedAndSavedCorrectly() = runTest(testDispatcher) {
        coEvery { readingRepository.getLatestReadingSync(1L) } returns null

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isInitialReading)
        assertNull(state.previousReadingValue)

        viewModel.onReadingInputChange("1500.0")
        val customInstant = DateUtils.toInstantAtDhaka(2026, 11, 2, 9, 0)
        viewModel.onDateChange(customInstant)
        viewModel.saveReading()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            readingRepository.saveReading(match {
                it.readingValue == BigDecimal("1500.0") &&
                    it.recordedAt == customInstant &&
                    it.note == "Initial Reading"
            })
            meterRepository.updateMeter(match {
                it.startReading == BigDecimal("1500.0")
            })
            settingsPreferences.setOnboardingCompleted(true)
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun onReadingInputChange_lowerValueShowsWarningDialogOnSave() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReadingInputChange("110.0")
        viewModel.saveReading(forceSave = false)

        val state = viewModel.uiState.value
        assertTrue(state.showConfirmDialog)
        assertFalse(state.isSaved)
    }

    @Test
    fun saveReading_forceSaveSavesSuccessfully() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReadingInputChange("110.0")
        viewModel.onNoteChange("Meter replaced")
        viewModel.saveReading(forceSave = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            readingRepository.saveReading(match {
                it.readingValue == BigDecimal("110.0") && it.note == "Meter replaced"
            })
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun onMeterResetChange_calculatesDeltaAsTotalValue() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReadingInputChange("5.0")
        viewModel.onMeterResetChange(true)

        val state = viewModel.uiState.value
        assertTrue(state.isMeterReset)
        assertEquals(BigDecimal("5.0"), state.calculatedDelta)
    }

    @Test
    fun onReadingInputChange_whenInSlab2_calculatesCostAtSlab2Rate() = runTest(testDispatcher) {
        val now = DateUtils.currentInstant()
        val ldt = now.toLocalDateTime(DateUtils.DHAKA_ZONE)
        val reading1Time = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 1, 9, 0)
        val reading2Time = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 5, 9, 0)
        val todayReadingTime = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 10, 9, 0)

        // Reading 1: 100.0, Reading 2: 200.0 (+100.0 units consumed so far in current month -> Slab 2)
        val existingReadings = listOf(
            Reading(
                id = 1L,
                meterId = 1L,
                readingValue = BigDecimal("100.0"),
                recordedAt = reading1Time,
                note = "Cycle start",
                isEstimated = false,
                isMeterReset = false,
                createdAt = reading1Time,
                updatedAt = reading1Time
            ),
            Reading(
                id = 2L,
                meterId = 1L,
                readingValue = BigDecimal("200.0"),
                recordedAt = reading2Time,
                note = "Mid month",
                isEstimated = false,
                isMeterReset = false,
                createdAt = reading2Time,
                updatedAt = reading2Time
            )
        )

        io.mockk.every { readingRepository.getReadings(1L) } returns flowOf(existingReadings)
        io.mockk.every { settingsPreferences.billingCycleDay } returns flowOf(1)
        io.mockk.every { settingsPreferences.appLanguage } returns flowOf("en")
        io.mockk.every { settingsPreferences.useBengaliDigits } returns flowOf(false)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDateChange(todayReadingTime)
        testDispatcher.scheduler.advanceUntilIdle()

        // User enters 220.0 (delta = 20.0 units)
        // Prior units = 100.0 (in Slab 2: 76..200 @ 6.63)
        // Cumulative after = 120.0 (in Slab 2)
        // 20.0 units * 6.63 = 132.60
        viewModel.onReadingInputChange("220.0")

        val state = viewModel.uiState.value
        assertEquals(BigDecimal("20.0"), state.calculatedDelta)
        assertEquals(BigDecimal("132.60"), state.estimatedDeltaCost)
        assertEquals(2, state.slabNumber)
        assertEquals("2nd Slab", state.slabLabel)
        assertEquals(BigDecimal("6.63"), state.slabRate)
        assertEquals(BigDecimal("120.0"), state.cumulativeMonthUnits)
    }

    @Test
    fun onReadingInputChange_whenBangla_formatsBengaliSlabLabel() = runTest(testDispatcher) {
        val now = DateUtils.currentInstant()
        val ldt = now.toLocalDateTime(DateUtils.DHAKA_ZONE)
        val reading1Time = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 1, 9, 0)
        val reading2Time = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 5, 9, 0)
        val todayReadingTime = DateUtils.toInstantAtDhaka(ldt.year, ldt.monthNumber, 10, 9, 0)

        val existingReadings = listOf(
            Reading(
                id = 1L,
                meterId = 1L,
                readingValue = BigDecimal("100.0"),
                recordedAt = reading1Time,
                note = "Cycle start",
                isEstimated = false,
                isMeterReset = false,
                createdAt = reading1Time,
                updatedAt = reading1Time
            ),
            Reading(
                id = 2L,
                meterId = 1L,
                readingValue = BigDecimal("200.0"),
                recordedAt = reading2Time,
                note = "Mid month",
                isEstimated = false,
                isMeterReset = false,
                createdAt = reading2Time,
                updatedAt = reading2Time
            )
        )

        io.mockk.every { readingRepository.getReadings(1L) } returns flowOf(existingReadings)
        io.mockk.every { settingsPreferences.billingCycleDay } returns flowOf(1)
        io.mockk.every { settingsPreferences.appLanguage } returns flowOf("bn")
        io.mockk.every { settingsPreferences.useBengaliDigits } returns flowOf(true)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDateChange(todayReadingTime)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReadingInputChange("220.0")

        val state = viewModel.uiState.value
        assertEquals("২য় স্ল্যাব", state.slabLabel)
        assertEquals(2, state.slabNumber)
    }
}
