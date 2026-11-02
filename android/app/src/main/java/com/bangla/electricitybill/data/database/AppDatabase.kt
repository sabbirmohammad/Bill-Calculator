package com.bangla.electricitybill.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bangla.electricitybill.data.dao.BillingPeriodDao
import com.bangla.electricitybill.data.dao.MeterDao
import com.bangla.electricitybill.data.dao.MeterEventDao
import com.bangla.electricitybill.data.dao.MeterReadingDao
import com.bangla.electricitybill.data.dao.MonthlySummaryDao
import com.bangla.electricitybill.data.dao.TariffDao
import com.bangla.electricitybill.data.entity.AdditionalChargeEntity
import com.bangla.electricitybill.data.entity.BillingPeriodEntity
import com.bangla.electricitybill.data.entity.MeterEntity
import com.bangla.electricitybill.data.entity.MeterEventEntity
import com.bangla.electricitybill.data.entity.MeterReadingEntity
import com.bangla.electricitybill.data.entity.MonthlySummaryEntity
import com.bangla.electricitybill.data.entity.TariffEntity
import com.bangla.electricitybill.data.entity.TariffSlabEntity

@Database(
    entities = [
        MeterEntity::class,
        MeterReadingEntity::class,
        BillingPeriodEntity::class,
        TariffEntity::class,
        TariffSlabEntity::class,
        AdditionalChargeEntity::class,
        MonthlySummaryEntity::class,
        MeterEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun meterReadingDao(): MeterReadingDao
    abstract fun billingPeriodDao(): BillingPeriodDao
    abstract fun tariffDao(): TariffDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao
    abstract fun meterEventDao(): MeterEventDao
}
