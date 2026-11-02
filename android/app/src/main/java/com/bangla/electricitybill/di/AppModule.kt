package com.bangla.electricitybill.di

import android.content.Context
import androidx.room.Room
import com.bangla.electricitybill.data.dao.BillingPeriodDao
import com.bangla.electricitybill.data.dao.MeterDao
import com.bangla.electricitybill.data.dao.MeterEventDao
import com.bangla.electricitybill.data.dao.MeterReadingDao
import com.bangla.electricitybill.data.dao.MonthlySummaryDao
import com.bangla.electricitybill.data.dao.TariffDao
import com.bangla.electricitybill.data.database.AppDatabase
import com.bangla.electricitybill.data.repository.BillingPeriodRepositoryImpl
import com.bangla.electricitybill.data.repository.MeterReadingRepositoryImpl
import com.bangla.electricitybill.data.repository.MeterRepositoryImpl
import com.bangla.electricitybill.data.repository.TariffRepositoryImpl
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "electricity_meter_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMeterDao(database: AppDatabase): MeterDao = database.meterDao()

    @Provides
    fun provideMeterReadingDao(database: AppDatabase): MeterReadingDao = database.meterReadingDao()

    @Provides
    fun provideBillingPeriodDao(database: AppDatabase): BillingPeriodDao = database.billingPeriodDao()

    @Provides
    fun provideTariffDao(database: AppDatabase): TariffDao = database.tariffDao()

    @Provides
    fun provideMonthlySummaryDao(database: AppDatabase): MonthlySummaryDao = database.monthlySummaryDao()

    @Provides
    fun provideMeterEventDao(database: AppDatabase): MeterEventDao = database.meterEventDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMeterRepository(impl: MeterRepositoryImpl): MeterRepository

    @Binds
    @Singleton
    abstract fun bindMeterReadingRepository(impl: MeterReadingRepositoryImpl): MeterReadingRepository

    @Binds
    @Singleton
    abstract fun bindTariffRepository(impl: TariffRepositoryImpl): TariffRepository

    @Binds
    @Singleton
    abstract fun bindBillingPeriodRepository(impl: BillingPeriodRepositoryImpl): BillingPeriodRepository
}
