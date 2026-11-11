package com.powerunit.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.reminder.ReminderNotificationHelper
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.toLocalDateTime

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val settingsPreferences: SettingsPreferences,
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val isEnabled = settingsPreferences.reminderEnabled.first()
        if (!isEnabled) {
            return Result.success()
        }

        // Skip notification if today's reading is already recorded
        val activeMeter = meterRepository.getActiveMeterSync()
        if (activeMeter != null) {
            val latest = readingRepository.getLatestReadingSync(activeMeter.id)
            if (latest != null) {
                val latestDate = latest.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).date
                val today = DateUtils.currentDhakaDate()
                if (latestDate == today) {
                    return Result.success()
                }
            }
        }

        ReminderNotificationHelper.showReminderNotification(context)
        return Result.success()
    }
}
