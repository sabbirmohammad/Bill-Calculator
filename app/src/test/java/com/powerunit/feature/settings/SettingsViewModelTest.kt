package com.powerunit.feature.settings

import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.worker.WorkManagerScheduler
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.repository.MeterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private val workManagerScheduler = mockk<WorkManagerScheduler>(relaxed = true)
    private val database = mockk<PowerUnitDatabase>(relaxed = true)
    private val readingDao = mockk<ReadingDao>(relaxed = true)

    private val sampleMeter = Meter(
        id = 1L,
        name = "Home Meter",
        type = MeterType.POSTPAID,
        startReading = BigDecimal("100.0"),
        multiplierFactor = BigDecimal.ONE,
        isActive = true,
        createdAt = Instant.fromEpochMilliseconds(1000L)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { database.readingDao() } returns readingDao
        coEvery { meterRepository.getActiveMeterSync() } returns sampleMeter
        coEvery { settingsPreferences.billingCycleDay } returns flowOf(1)
        coEvery { settingsPreferences.reminderEnabled } returns flowOf(true)
        coEvery { settingsPreferences.reminderTime } returns flowOf("21:00")
        coEvery { settingsPreferences.themeMode } returns flowOf("SYSTEM")
        coEvery { settingsPreferences.appLanguage } returns flowOf("en")
        coEvery { settingsPreferences.useBengaliDigits } returns flowOf(false)
        coEvery { settingsPreferences.appLockEnabled } returns flowOf(false)
        coEvery { settingsPreferences.hideAmounts } returns flowOf(false)
        coEvery { settingsPreferences.screenshotBlock } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            meterRepository = meterRepository,
            settingsPreferences = settingsPreferences,
            workManagerScheduler = workManagerScheduler,
            database = database
        )
    }

    @Test
    fun loadSettings_initializesStateFromPreferences() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Home Meter", state.activeMeter?.name)
        assertEquals(1, state.billingCycleDay)
        assertTrue(state.reminderEnabled)
        assertEquals("21:00", state.reminderTime)
        assertEquals("en", state.language)
        assertFalse(state.useBengaliDigits)
    }

    @Test
    fun setBillingCycleDay_callsPreferences() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setBillingCycleDay(15)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setBillingCycleDay(15) }
    }

    @Test
    fun setReminderEnabled_false_cancelsDailyReminder() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setReminderEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsPreferences.setReminderEnabled(false) }
        verify { workManagerScheduler.cancelDailyReminder() }
    }

    @Test
    fun clearAllData_deletesReadingsFromDatabase() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearAllData()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { readingDao.deleteAllReadings() }
        assertTrue(viewModel.uiState.value.clearDataSuccess)
    }
}
