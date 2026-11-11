package com.powerunit.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.core.worker.WorkManagerScheduler
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class DailyReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    @Inject
    lateinit var meterRepository: MeterRepository

    @Inject
    lateinit var readingRepository: ReadingRepository

    @Inject
    lateinit var workManagerScheduler: WorkManagerScheduler

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.powerunit.ACTION_TRIGGER_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = settingsPreferences.reminderEnabled.first()
                val reminderTime = settingsPreferences.reminderTime.first()

                if (action == ACTION_TRIGGER_REMINDER) {
                    if (isEnabled) {
                        var alreadyRecordedToday = false
                        val activeMeter = meterRepository.getActiveMeterSync()
                        if (activeMeter != null) {
                            val latest = readingRepository.getLatestReadingSync(activeMeter.id)
                            if (latest != null) {
                                val latestDate = latest.recordedAt.toLocalDateTime(DateUtils.DHAKA_ZONE).date
                                val today = DateUtils.currentDhakaDate()
                                if (latestDate == today) {
                                    alreadyRecordedToday = true
                                }
                            }
                        }

                        if (!alreadyRecordedToday) {
                            ReminderNotificationHelper.showReminderNotification(context)
                        }

                        // Reschedule for next day
                        workManagerScheduler.scheduleDailyReminder(reminderTime)
                    }
                } else if (action == Intent.ACTION_BOOT_COMPLETED ||
                    action == Intent.ACTION_TIME_CHANGED ||
                    action == Intent.ACTION_TIMEZONE_CHANGED ||
                    action == Intent.ACTION_MY_PACKAGE_REPLACED
                ) {
                    if (isEnabled) {
                        workManagerScheduler.scheduleDailyReminder(reminderTime)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
