package com.bangla.electricitybill.data.backup

import androidx.room.withTransaction
import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.database.AppDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: AppDatabase
) {

    private val gson: Gson = BackupGson.create()

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val payload = AppBackupPayload(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            meters = database.meterDao().getAllMeters(),
            meterReadings = database.meterReadingDao().getAllReadings(),
            billingPeriods = database.billingPeriodDao().getAllPeriods(),
            tariffs = database.tariffDao().getAllTariffs(),
            tariffSlabs = database.tariffDao().getAllSlabs(),
            additionalCharges = database.tariffDao().getAllAdditionalCharges(),
            monthlySummaries = database.monthlySummaryDao().getAllSummaries(),
            meterEvents = database.meterEventDao().getAllEvents()
        )
        gson.toJson(payload)
    }

    suspend fun restoreFromJson(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload = gson.fromJson(jsonString, AppBackupPayload::class.java)
                ?: return@withContext Result.failure(IllegalArgumentException("অবৈধ ব্যাকআপ ফাইল (Invalid backup JSON)"))

            database.withTransaction {
                for (meter in payload.meters) {
                    database.meterDao().insertMeter(meter)
                }
                for (tariff in payload.tariffs) {
                    database.tariffDao().insertTariff(tariff)
                }
                database.tariffDao().insertSlabs(payload.tariffSlabs)
                database.tariffDao().insertAdditionalCharges(payload.additionalCharges)

                for (period in payload.billingPeriods) {
                    database.billingPeriodDao().insertPeriod(period)
                }
                database.meterReadingDao().insertAll(payload.meterReadings)

                for (summary in payload.monthlySummaries) {
                    database.monthlySummaryDao().insertSummary(summary)
                }
                for (event in payload.meterEvents) {
                    database.meterEventDao().insertEvent(event)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportReadingsToCsv(): String = withContext(Dispatchers.IO) {
        val readings = database.meterReadingDao().getAllReadings().sortedBy { it.readingDateTime }
        val sb = StringBuilder()
        sb.append("ID,Date,Time,BlackDigits,RedDigit,TotalKwh,IsBaseline,Source,Note\n")
        for (r in readings) {
            val date = DateUtils.formatDate(r.readingDateTime)
            val time = DateUtils.formatTime(r.readingDateTime)
            val escapedNote = r.note?.replace("\"", "\"\"") ?: ""
            sb.append("\"${r.id}\",\"$date\",\"$time\",\"${r.blackDigits}\",\"${r.redDigit}\",\"${r.normalizedReading}\",\"${r.isBaseline}\",\"${r.readingSource}\",\"$escapedNote\"\n")
        }
        sb.toString()
    }
}
