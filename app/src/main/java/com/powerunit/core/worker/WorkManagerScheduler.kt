package com.powerunit.core.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val REMINDER_WORK_NAME = "wattwise_daily_reminder"
        const val BUDGET_WORK_NAME = "wattwise_budget_alert"
    }

    fun scheduleDailyReminder(reminderTime: String) {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelayMinutes = (target.timeInMillis - now.timeInMillis) / (1000 * 60)

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun scheduleBudgetAlert() {
        val workRequest = PeriodicWorkRequestBuilder<BudgetAlertWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BUDGET_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
