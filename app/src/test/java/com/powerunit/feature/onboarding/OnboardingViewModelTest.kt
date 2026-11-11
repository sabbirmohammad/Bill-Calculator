package com.powerunit.feature.onboarding

import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.repository.MeterRepository
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
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(meterRepository, settingsPreferences)
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
    fun completeOnboarding_savesMeterAndUpdatesPreferences() = runTest(testDispatcher) {
        viewModel.onMeterNameChange("Apartment Meter")
        viewModel.onMeterTypeChange(MeterType.POSTPAID)
        viewModel.onInitialReadingChange("1250.0")
        viewModel.onCycleDayChange(15)

        viewModel.completeOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            meterRepository.saveMeter(match {
                it.name == "Apartment Meter" &&
                    it.startReading == BigDecimal("1250.0") &&
                    it.type == MeterType.POSTPAID
            })
            settingsPreferences.setBillingCycleDay(15)
            settingsPreferences.setOnboardingCompleted(true)
        }

        assertTrue(viewModel.uiState.value.isCompleted)
    }
}
