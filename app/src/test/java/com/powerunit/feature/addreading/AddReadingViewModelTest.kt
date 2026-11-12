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
}
