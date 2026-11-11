package com.powerunit.feature.onboarding

import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { meterRepository.saveMeter(any()) } returns 1L
        viewModel = OnboardingViewModel(meterRepository, readingRepository, settingsPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onNextStep_incrementsStep() {
        assertEquals(0, viewModel.uiState.value.step)
        viewModel.onNextStep()
        assertEquals(1, viewModel.uiState.value.step)
        viewModel.onPreviousStep()
        assertEquals(0, viewModel.uiState.value.step)
    }

    @Test
    fun completeOnboarding_savesMeterAndReadingWithCustomDate() = runTest(testDispatcher) {
        val customInstant = DateUtils.toInstantAtDhaka(2026, 11, 1, 9, 30)

        viewModel.onMeterNameChange("Apartment Meter")
        viewModel.onMeterTypeChange(MeterType.POSTPAID)
        viewModel.onInitialReadingChange("1250.0")
        viewModel.onInitialReadingDateChange(customInstant)
        viewModel.onCycleDayChange(15)

        viewModel.completeOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            meterRepository.saveMeter(match {
                it.name == "Apartment Meter" &&
                    it.startReading == BigDecimal("1250.0") &&
                    it.type == MeterType.POSTPAID
            })
            readingRepository.saveReading(match {
                it.readingValue == BigDecimal("1250.0") &&
                    it.recordedAt == customInstant &&
                    it.note == "Initial Reading"
            })
            settingsPreferences.setBillingCycleDay(15)
            settingsPreferences.setOnboardingCompleted(true)
        }

        assertTrue(viewModel.uiState.value.isCompleted)
    }
}
