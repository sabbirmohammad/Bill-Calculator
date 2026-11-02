package com.bangla.electricitybill.data.repository

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.data.dao.BillingPeriodDao
import com.bangla.electricitybill.data.dao.MeterDao
import com.bangla.electricitybill.data.dao.MeterEventDao
import com.bangla.electricitybill.data.dao.MeterReadingDao
import com.bangla.electricitybill.data.dao.MonthlySummaryDao
import com.bangla.electricitybill.data.dao.TariffDao
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.model.PeriodStatus
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterRepositoryImpl @Inject constructor(
    private val meterDao: MeterDao,
    private val meterEventDao: MeterEventDao
) : MeterRepository {

    override fun getActiveMeterFlow(): Flow<Meter?> {
        return meterDao.getActiveMeterFlow().map { it?.toDomain() }
    }

    override suspend fun getActiveMeter(): Meter? {
        return meterDao.getActiveMeter()?.toDomain()
    }

    override suspend fun getMeterById(id: String): Meter? {
        return meterDao.getMeterById(id)?.toDomain()
    }

    override suspend fun saveMeter(meter: Meter) {
        meterDao.insertMeter(meter.toEntity())
    }

    override suspend fun updateMeter(meter: Meter) {
        meterDao.updateMeter(meter.toEntity())
    }

    override suspend fun logMeterEvent(event: MeterEvent) {
        meterEventDao.insertEvent(event.toEntity())
    }

    override fun getMeterEventsFlow(meterId: String): Flow<List<MeterEvent>> {
        return meterEventDao.getEventsForMeterFlow(meterId).map { list ->
            list.map { it.toDomain() }
        }
    }
}

@Singleton
class MeterReadingRepositoryImpl @Inject constructor(
    private val meterReadingDao: MeterReadingDao
) : MeterReadingRepository {

    override fun getReadingsFlow(meterId: String): Flow<List<MeterReading>> {
        return meterReadingDao.getReadingsForMeterFlow(meterId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getReadings(meterId: String): List<MeterReading> {
        return meterReadingDao.getReadingsForMeter(meterId).map { it.toDomain() }
    }

    override fun getLatestReadingFlow(meterId: String): Flow<MeterReading?> {
        return meterReadingDao.getLatestReadingFlow(meterId).map { it?.toDomain() }
    }

    override suspend fun getLatestReading(meterId: String): MeterReading? {
        return meterReadingDao.getLatestReading(meterId)?.toDomain()
    }

    override suspend fun getBaselineReading(meterId: String): MeterReading? {
        return meterReadingDao.getBaselineReading(meterId)?.toDomain()
    }

    override suspend fun getPreviousReading(meterId: String, dateTime: LocalDateTime): MeterReading? {
        return meterReadingDao.getPreviousReading(meterId, dateTime)?.toDomain()
    }

    override suspend fun getNextReading(meterId: String, dateTime: LocalDateTime): MeterReading? {
        return meterReadingDao.getNextReading(meterId, dateTime)?.toDomain()
    }

    override suspend fun getReadingById(id: String): MeterReading? {
        return meterReadingDao.getReadingById(id)?.toDomain()
    }

    override suspend fun saveReading(reading: MeterReading) {
        meterReadingDao.insertReading(reading.toEntity())
    }

    override suspend fun deleteReading(id: String) {
        meterReadingDao.deleteReadingById(id)
    }
}

@Singleton
class TariffRepositoryImpl @Inject constructor(
    private val tariffDao: TariffDao
) : TariffRepository {

    override fun getActiveTariffFlow(): Flow<Tariff?> {
        return tariffDao.getActiveTariffFlow().flatMapLatest { tariffEntity ->
            if (tariffEntity == null) {
                flowOf(null)
            } else {
                combine(
                    tariffDao.getSlabsForTariffFlow(tariffEntity.id),
                    tariffDao.getAdditionalChargesForTariffFlow(tariffEntity.id)
                ) { slabs, charges ->
                    tariffEntity.toDomain(slabs, charges)
                }
            }
        }
    }

    override suspend fun getActiveTariff(): Tariff? {
        val tariffEntity = tariffDao.getActiveTariff() ?: return null
        val slabs = tariffDao.getSlabsForTariff(tariffEntity.id)
        val charges = tariffDao.getAdditionalChargesForTariff(tariffEntity.id)
        return tariffEntity.toDomain(slabs, charges)
    }

    override suspend fun getTariffById(id: String): Tariff? {
        val tariffEntity = tariffDao.getTariffById(id) ?: return null
        val slabs = tariffDao.getSlabsForTariff(id)
        val charges = tariffDao.getAdditionalChargesForTariff(id)
        return tariffEntity.toDomain(slabs, charges)
    }

    override suspend fun saveTariff(tariff: Tariff) {
        tariffDao.insertTariff(tariff.toEntity())
        tariffDao.insertSlabs(tariff.slabs.map { it.toEntity() })
        tariffDao.insertAdditionalCharges(tariff.additionalCharges.map { it.toEntity() })
        if (tariff.isActive) {
            tariffDao.deactivateOtherTariffs(tariff.id)
        }
    }

    override suspend fun setActiveTariff(tariffId: String) {
        tariffDao.deactivateOtherTariffs(tariffId)
    }
}

@Singleton
class BillingPeriodRepositoryImpl @Inject constructor(
    private val billingPeriodDao: BillingPeriodDao,
    private val monthlySummaryDao: MonthlySummaryDao
) : BillingPeriodRepository {

    override fun getBillingPeriodsFlow(meterId: String): Flow<List<BillingPeriod>> {
        return billingPeriodDao.getPeriodsForMeterFlow(meterId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getBillingPeriodById(id: String): BillingPeriod? {
        return billingPeriodDao.getPeriodById(id)?.toDomain()
    }

    override suspend fun getOrCreatePeriodForDateTime(
        meterId: String,
        dateTime: LocalDateTime,
        tariffId: String
    ): BillingPeriod {
        val yearMonth = YearMonth.from(dateTime)
        val periodId = DateUtils.getCycleId(meterId, yearMonth)
        val existing = billingPeriodDao.getPeriodById(periodId)
        if (existing != null) {
            return existing.toDomain()
        }

        val periodStart = DateUtils.getMonthStart(yearMonth)
        val periodEnd = DateUtils.getMonthEnd(yearMonth)
        val now = DateUtils.now()

        val newPeriod = BillingPeriod(
            id = periodId,
            meterId = meterId,
            periodStart = periodStart,
            periodEnd = periodEnd,
            openingReading = null,
            closingReading = null,
            tariffVersionId = tariffId,
            periodStatus = PeriodStatus.IN_PROGRESS,
            trackingStartDate = null,
            createdAt = now,
            completedAt = null
        )
        billingPeriodDao.insertPeriod(newPeriod.toEntity())
        return newPeriod
    }

    override suspend fun updateBillingPeriod(period: BillingPeriod) {
        billingPeriodDao.updatePeriod(period.toEntity())
    }

    override fun getMonthlySummaryFlow(periodId: String): Flow<MonthlySummary?> {
        return monthlySummaryDao.getSummaryForPeriodFlow(periodId).map { it?.toDomain() }
    }

    override suspend fun getMonthlySummary(periodId: String): MonthlySummary? {
        return monthlySummaryDao.getSummaryForPeriod(periodId)?.toDomain()
    }

    override suspend fun saveMonthlySummary(summary: MonthlySummary) {
        monthlySummaryDao.insertSummary(summary.toEntity())
    }
}
