package com.bangla.electricitybill.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bangla.electricitybill.data.entity.AdditionalChargeEntity
import com.bangla.electricitybill.data.entity.BillingPeriodEntity
import com.bangla.electricitybill.data.entity.MeterEntity
import com.bangla.electricitybill.data.entity.MeterEventEntity
import com.bangla.electricitybill.data.entity.MeterReadingEntity
import com.bangla.electricitybill.data.entity.MonthlySummaryEntity
import com.bangla.electricitybill.data.entity.TariffEntity
import com.bangla.electricitybill.data.entity.TariffSlabEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MeterDao {
    @Query("SELECT * FROM meters WHERE meterStatus = 'ACTIVE' LIMIT 1")
    fun getActiveMeterFlow(): Flow<MeterEntity?>

    @Query("SELECT * FROM meters WHERE meterStatus = 'ACTIVE' LIMIT 1")
    suspend fun getActiveMeter(): MeterEntity?

    @Query("SELECT * FROM meters WHERE id = :id")
    suspend fun getMeterById(id: String): MeterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeter(meter: MeterEntity)

    @Update
    suspend fun updateMeter(meter: MeterEntity)

    @Query("SELECT * FROM meters ORDER BY createdAt DESC")
    fun getAllMetersFlow(): Flow<List<MeterEntity>>

    @Query("SELECT * FROM meters")
    suspend fun getAllMeters(): List<MeterEntity>
}

@Dao
interface MeterReadingDao {
    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId ORDER BY readingDateTime ASC")
    fun getReadingsForMeterFlow(meterId: String): Flow<List<MeterReadingEntity>>

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId ORDER BY readingDateTime ASC")
    suspend fun getReadingsForMeter(meterId: String): List<MeterReadingEntity>

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId ORDER BY readingDateTime DESC LIMIT 1")
    fun getLatestReadingFlow(meterId: String): Flow<MeterReadingEntity?>

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId ORDER BY readingDateTime DESC LIMIT 1")
    suspend fun getLatestReading(meterId: String): MeterReadingEntity?

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND isBaseline = 1 LIMIT 1")
    suspend fun getBaselineReading(meterId: String): MeterReadingEntity?

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND readingDateTime < :dateTime ORDER BY readingDateTime DESC LIMIT 1")
    suspend fun getPreviousReading(meterId: String, dateTime: LocalDateTime): MeterReadingEntity?

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND readingDateTime > :dateTime ORDER BY readingDateTime ASC LIMIT 1")
    suspend fun getNextReading(meterId: String, dateTime: LocalDateTime): MeterReadingEntity?

    @Query("SELECT * FROM meter_readings WHERE meterId = :meterId AND readingDateTime BETWEEN :start AND :end ORDER BY readingDateTime ASC")
    suspend fun getReadingsBetween(meterId: String, start: LocalDateTime, end: LocalDateTime): List<MeterReadingEntity>

    @Query("SELECT * FROM meter_readings WHERE id = :id")
    suspend fun getReadingById(id: String): MeterReadingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: MeterReadingEntity)

    @Update
    suspend fun updateReading(reading: MeterReadingEntity)

    @Query("DELETE FROM meter_readings WHERE id = :id")
    suspend fun deleteReadingById(id: String)

    @Query("SELECT * FROM meter_readings")
    suspend fun getAllReadings(): List<MeterReadingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<MeterReadingEntity>)
}

@Dao
interface BillingPeriodDao {
    @Query("SELECT * FROM billing_periods WHERE meterId = :meterId ORDER BY periodStart DESC")
    fun getPeriodsForMeterFlow(meterId: String): Flow<List<BillingPeriodEntity>>

    @Query("SELECT * FROM billing_periods WHERE id = :id")
    suspend fun getPeriodById(id: String): BillingPeriodEntity?

    @Query("SELECT * FROM billing_periods WHERE meterId = :meterId AND :dateTime BETWEEN periodStart AND periodEnd LIMIT 1")
    suspend fun getPeriodForDateTime(meterId: String, dateTime: LocalDateTime): BillingPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: BillingPeriodEntity)

    @Update
    suspend fun updatePeriod(period: BillingPeriodEntity)

    @Query("SELECT * FROM billing_periods")
    suspend fun getAllPeriods(): List<BillingPeriodEntity>
}

@Dao
interface TariffDao {
    @Query("SELECT * FROM tariffs WHERE isActive = 1 LIMIT 1")
    fun getActiveTariffFlow(): Flow<TariffEntity?>

    @Query("SELECT * FROM tariffs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTariff(): TariffEntity?

    @Query("SELECT * FROM tariffs WHERE id = :id")
    suspend fun getTariffById(id: String): TariffEntity?

    @Query("SELECT * FROM tariff_slabs WHERE tariffId = :tariffId ORDER BY displayOrder ASC")
    suspend fun getSlabsForTariff(tariffId: String): List<TariffSlabEntity>

    @Query("SELECT * FROM tariff_slabs WHERE tariffId = :tariffId ORDER BY displayOrder ASC")
    fun getSlabsForTariffFlow(tariffId: String): Flow<List<TariffSlabEntity>>

    @Query("SELECT * FROM additional_charges WHERE tariffId = :tariffId")
    suspend fun getAdditionalChargesForTariff(tariffId: String): List<AdditionalChargeEntity>

    @Query("SELECT * FROM additional_charges WHERE tariffId = :tariffId")
    fun getAdditionalChargesForTariffFlow(tariffId: String): Flow<List<AdditionalChargeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTariff(tariff: TariffEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlabs(slabs: List<TariffSlabEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdditionalCharges(charges: List<AdditionalChargeEntity>)

    @Query("UPDATE tariffs SET isActive = 0 WHERE id != :activeId")
    suspend fun deactivateOtherTariffs(activeId: String)

    @Query("SELECT * FROM tariffs")
    suspend fun getAllTariffs(): List<TariffEntity>

    @Query("SELECT * FROM tariff_slabs")
    suspend fun getAllSlabs(): List<TariffSlabEntity>

    @Query("SELECT * FROM additional_charges")
    suspend fun getAllAdditionalCharges(): List<AdditionalChargeEntity>
}

@Dao
interface MonthlySummaryDao {
    @Query("SELECT * FROM monthly_summaries WHERE billingPeriodId = :periodId LIMIT 1")
    suspend fun getSummaryForPeriod(periodId: String): MonthlySummaryEntity?

    @Query("SELECT * FROM monthly_summaries WHERE billingPeriodId = :periodId LIMIT 1")
    fun getSummaryForPeriodFlow(periodId: String): Flow<MonthlySummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: MonthlySummaryEntity)

    @Query("SELECT * FROM monthly_summaries")
    suspend fun getAllSummaries(): List<MonthlySummaryEntity>
}

@Dao
interface MeterEventDao {
    @Query("SELECT * FROM meter_events WHERE meterId = :meterId ORDER BY eventDate DESC")
    fun getEventsForMeterFlow(meterId: String): Flow<List<MeterEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MeterEventEntity)

    @Query("SELECT * FROM meter_events")
    suspend fun getAllEvents(): List<MeterEventEntity>
}
