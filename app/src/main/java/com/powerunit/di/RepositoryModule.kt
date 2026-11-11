package com.powerunit.di

import com.powerunit.data.repository.BudgetRepositoryImpl
import com.powerunit.data.repository.MeterRepositoryImpl
import com.powerunit.data.repository.ReadingRepositoryImpl
import com.powerunit.data.repository.RechargeRepositoryImpl
import com.powerunit.data.repository.TariffRepositoryImpl
import com.powerunit.domain.repository.BudgetRepository
import com.powerunit.domain.repository.MeterRepository
import com.powerunit.domain.repository.ReadingRepository
import com.powerunit.domain.repository.RechargeRepository
import com.powerunit.domain.repository.TariffRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMeterRepository(impl: MeterRepositoryImpl): MeterRepository

    @Binds
    @Singleton
    abstract fun bindReadingRepository(impl: ReadingRepositoryImpl): ReadingRepository

    @Binds
    @Singleton
    abstract fun bindTariffRepository(impl: TariffRepositoryImpl): TariffRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindRechargeRepository(impl: RechargeRepositoryImpl): RechargeRepository
}
