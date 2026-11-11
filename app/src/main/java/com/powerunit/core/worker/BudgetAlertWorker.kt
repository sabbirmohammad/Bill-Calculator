package com.powerunit.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.powerunit.MainActivity
import com.powerunit.R
import com.powerunit.core.common.DateUtils
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.domain.repository.BudgetRepository
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.TariffRepository
import com.powerunit.domain.usecase.CalculateBillUseCase
import com.powerunit.domain.usecase.CalculateCycleUsageUseCase
import com.powerunit.domain.usecase.CalculateDailyUsageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val meterRepository: MeterRepository,
    private val readingRepository: ReadingRepository,
    private val tariffRepository: TariffRepository,
    private val budgetRepository: BudgetRepository,
    private val settingsPreferences: SettingsPreferences,
    private val calculateDailyUsageUseCase: CalculateDailyUsageUseCase,
    private val calculateCycleUsageUseCase: CalculateCycleUsageUseCase,
    private val calculateBillUseCase: CalculateBillUseCase
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "budget_alerts_channel"
        const val NOTIFICATION_ID = 1002
    }

    override suspend fun doWork(): Result {
        val ratio = checkBudgetExceededRatio()
        if (ratio != null && ratio >= 0.8) {
            val pctString = (ratio * 100).toInt().toString()
            sendAlertNotification(pctString)
        }
        return Result.success()
    }

    private suspend fun checkBudgetExceededRatio(): Double? {
        val meter = meterRepository.getActiveMeterSync() ?: return null
        val readings = readingRepository.getReadings(meter.id).first()
        val tariff = tariffRepository.getActiveTariffSync()
        val budget = budgetRepository.getLatestBudgetSync()
        val limitBDT = budget?.amountLimit

        if (readings.size < 2 || tariff == null || limitBDT == null || limitBDT <= BigDecimal.ZERO) {
            return null
        }

        val cycleStartDay = settingsPreferences.billingCycleDay.first()
        val sorted = readings.sortedBy { it.recordedAt }
        val dailyUsages = calculateDailyUsageUseCase(sorted)
        val cycleUsage = calculateCycleUsageUseCase(dailyUsages, cycleStartDay, DateUtils.currentDhakaDate())
        val bill = calculateBillUseCase(cycleUsage.totalUnitsSoFar, tariff)

        return bill.totalBill.divide(limitBDT, 2, RoundingMode.HALF_UP).toDouble()
    }

    private fun sendAlertNotification(percentUsed: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_alerts_desc)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_budget_warning_title))
            .setContentText(context.getString(R.string.notification_budget_warning_body, percentUsed))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
