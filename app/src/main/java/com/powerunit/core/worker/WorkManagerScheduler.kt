package com.powerunit.core.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.powerunit.core.reminder.DailyReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
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
        const val REMINDER_PENDING_INTENT_REQUEST_CODE = 2001
    }

    fun scheduleDailyReminder(reminderTime: String) {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        val initialDelayMinutes = (target.timeInMillis - now.timeInMillis) / (1000 * 60)

        // 1. AlarmManager exact trigger
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = DailyReminderReceiver.ACTION_TRIGGER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_PENDING_INTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        }

        // 2. Periodic WorkManager backup
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            val intent = Intent(context, DailyReminderReceiver::class.java).apply {
                action = DailyReminderReceiver.ACTION_TRIGGER_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_PENDING_INTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
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
