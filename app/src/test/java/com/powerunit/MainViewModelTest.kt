package com.powerunit

import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.repository.MeterRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private val meterRepository = mockk<MeterRepository>(relaxed = true)

    private val sampleMeter = Meter(
        id = 1L,
        name = "Home Meter",
        type = MeterType.POSTPAID,
        startReading = BigDecimal("100.0"),
        multiplierFactor = BigDecimal.ONE,
        isActive = true,
        createdAt = DateUtils.currentInstant()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { settingsPreferences.themeMode } returns flowOf("DARK")
        coEvery { settingsPreferences.appLanguage } returns flowOf("en")
        coEvery { settingsPreferences.onboardingCompleted } returns flowOf(true)
        coEvery { settingsPreferences.appLockEnabled } returns flowOf(false)
        coEvery { meterRepository.getActiveMeter() } returns flowOf(sampleMeter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isAppReady becomes true when preferences and initial data emit`() = runTest {
        val viewModel = MainViewModel(settingsPreferences, meterRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isAppReady.value)
        assertEquals("DARK", viewModel.themeMode.value)
        assertEquals("en", viewModel.appLanguage.value)
        assertTrue(viewModel.onboardingCompleted.value)
    }
}
