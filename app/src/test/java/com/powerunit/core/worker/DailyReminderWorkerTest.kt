package com.powerunit.core.worker

import android.content.Context
import androidx.work.WorkerParameters
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class DailyReminderWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val workerParams = mockk<WorkerParameters>(relaxed = true)
    private val settingsPreferences = mockk<SettingsPreferences>(relaxed = true)
    private val meterRepository = mockk<MeterRepository>(relaxed = true)
    private val readingRepository = mockk<ReadingRepository>(relaxed = true)

    @Test
    fun doWork_whenRemindersDisabled_returnsSuccessWithoutDispatch() = runTest {
        coEvery { settingsPreferences.reminderEnabled } returns flowOf(false)

        val worker = DailyReminderWorker(
            context,
            workerParams,
            settingsPreferences,
            meterRepository,
            readingRepository
        )

        val result = worker.doWork()
        assertEquals(androidx.work.ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_whenReadingAlreadyRecordedToday_skipsNotification() = runTest {
        coEvery { settingsPreferences.reminderEnabled } returns flowOf(true)

        val activeMeter = Meter(
            id = 1L,
            name = "Main Meter",
            type = MeterType.POSTPAID,
            startReading = BigDecimal("100.0"),
            multiplierFactor = BigDecimal.ONE,
            isActive = true,
            createdAt = DateUtils.currentInstant()
        )
        coEvery { meterRepository.getActiveMeterSync() } returns activeMeter

        val todayReading = Reading(
            id = 10L,
            meterId = 1L,
            readingValue = BigDecimal("150.0"),
            recordedAt = DateUtils.currentInstant(),
            isMeterReset = false,
            isEstimated = false,
            createdAt = DateUtils.currentInstant(),
            updatedAt = DateUtils.currentInstant()
        )
        coEvery { readingRepository.getLatestReadingSync(1L) } returns todayReading

        val worker = DailyReminderWorker(
            context,
            workerParams,
            settingsPreferences,
            meterRepository,
            readingRepository
        )

        val result = worker.doWork()
        assertEquals(androidx.work.ListenableWorker.Result.success(), result)
    }
}
