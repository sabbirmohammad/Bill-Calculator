package com.powerunit.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.dao.BudgetDao
import com.powerunit.core.database.dao.MeterDao
import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.core.database.dao.RechargeDao
import com.powerunit.core.database.dao.TariffDao
import com.powerunit.core.database.security.DatabasePassphraseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePowerUnitDatabase(
        @ApplicationContext context: Context,
        passphraseHelper: DatabasePassphraseHelper,
        databaseProvider: Provider<PowerUnitDatabase>
    ): PowerUnitDatabase {
        val passphrase = passphraseHelper.getOrCreatePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            PowerUnitDatabase::class.java,
            PowerUnitDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = databaseProvider.get()
                        val (tariff, slabs) = PowerUnitDatabase.createDefaultBangladeshTariff(
                            effectiveFrom = DateUtils.currentInstant()
                        )
                        val tariffId = database.tariffDao().insertTariff(tariff)
                        database.tariffDao().insertSlabs(slabs.map { it.copy(tariffId = tariffId) })
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideMeterDao(database: PowerUnitDatabase): MeterDao = database.meterDao()

    @Provides
    fun provideReadingDao(database: PowerUnitDatabase): ReadingDao = database.readingDao()

    @Provides
    fun provideTariffDao(database: PowerUnitDatabase): TariffDao = database.tariffDao()

    @Provides
    fun provideBudgetDao(database: PowerUnitDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRechargeDao(database: PowerUnitDatabase): RechargeDao = database.rechargeDao()
}
