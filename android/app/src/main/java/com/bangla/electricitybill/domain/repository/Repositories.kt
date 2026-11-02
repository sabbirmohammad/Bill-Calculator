package com.bangla.electricitybill.domain.repository

import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.model.Tariff
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MeterRepository {
    fun getActiveMeterFlow(): Flow<Meter?>
    suspend fun getActiveMeter(): Meter?
    suspend fun getMeterById(id: String): Meter?
    suspend fun saveMeter(meter: Meter)
    suspend fun updateMeter(meter: Meter)
    suspend fun logMeterEvent(event: MeterEvent)
    fun getMeterEventsFlow(meterId: String): Flow<List<MeterEvent>>
}

interface MeterReadingRepository {
    fun getReadingsFlow(meterId: String): Flow<List<MeterReading>>
    suspend fun getReadings(meterId: String): List<MeterReading>
    fun getLatestReadingFlow(meterId: String): Flow<MeterReading?>
    suspend fun getLatestReading(meterId: String): MeterReading?
    suspend fun getBaselineReading(meterId: String): MeterReading?
    suspend fun getPreviousReading(meterId: String, dateTime: LocalDateTime): MeterReading?
    suspend fun getNextReading(meterId: String, dateTime: LocalDateTime): MeterReading?
    suspend fun getReadingById(id: String): MeterReading?
    suspend fun saveReading(reading: MeterReading)
    suspend fun deleteReading(id: String)
}

interface TariffRepository {
    fun getActiveTariffFlow(): Flow<Tariff?>
    suspend fun getActiveTariff(): Tariff?
    suspend fun getTariffById(id: String): Tariff?
    suspend fun saveTariff(tariff: Tariff)
    suspend fun setActiveTariff(tariffId: String)
}

interface BillingPeriodRepository {
    fun getBillingPeriodsFlow(meterId: String): Flow<List<BillingPeriod>>
    suspend fun getBillingPeriodById(id: String): BillingPeriod?
    suspend fun getOrCreatePeriodForDateTime(meterId: String, dateTime: LocalDateTime, tariffId: String): BillingPeriod
    suspend fun updateBillingPeriod(period: BillingPeriod)
    fun getMonthlySummaryFlow(periodId: String): Flow<MonthlySummary?>
    suspend fun getMonthlySummary(periodId: String): MonthlySummary?
    suspend fun saveMonthlySummary(summary: MonthlySummary)
}
