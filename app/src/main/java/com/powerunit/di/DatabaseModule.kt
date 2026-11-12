package com.powerunit.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.dao.BudgetDao
import com.powerunit.core.database.dao.MeterDao
import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.core.database.dao.RechargeDao
import com.powerunit.core.database.dao.TariffDao
import com.powerunit.core.database.dao.TariffVersionDao
import com.powerunit.core.database.security.DatabasePassphraseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePowerUnitDatabase(
        @ApplicationContext context: Context,
        passphraseHelper: DatabasePassphraseHelper
    ): PowerUnitDatabase {
        try {
            System.loadLibrary("sqlcipher")
        } catch (_: UnsatisfiedLinkError) {
            // Handled or already loaded
        }

        val passphrase = passphraseHelper.getOrCreatePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            PowerUnitDatabase::class.java,
            PowerUnitDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(PowerUnitDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val nowMillis = System.currentTimeMillis()
                    db.execSQL(
                        "INSERT OR IGNORE INTO meters (id, name, type, startReading, multiplierFactor, isActive, createdAt) " +
                            "VALUES (1, 'Main Meter', 'POSTPAID', '0.0', '1.0', 1, $nowMillis)"
                    )
                    // Seed default tariff with epoch 0 so any user-created version takes precedence
                    db.execSQL(
                        "INSERT OR IGNORE INTO tariffs (id, name, effectiveFrom, vatPercent, serviceChargeFlat, demandChargeFlat, isActive) " +
                            "VALUES (1, 'Bangladesh Domestic Postpaid (LT-A)', 0, '5.0', '10.00', '35.00', 1)"
                    )
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '0', '75', '4.85')")
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '76', '200', '6.63')")
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '201', '300', '6.95')")
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '301', '400', '7.34')")
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '401', '600', '11.51')")
                    db.execSQL("INSERT OR IGNORE INTO tariff_slabs (tariffId, fromUnit, toUnit, ratePerUnit) VALUES (1, '601', NULL, '13.26')")

                    // Seed initial tariff_version with baseline cycle 1970-01
                    val defaultSlabsJson = """[{"from":"0","to":"75","rate":"4.85"},{"from":"76","to":"200","rate":"6.63"},{"from":"201","to":"300","rate":"6.95"},{"from":"301","to":"400","rate":"7.34"},{"from":"401","to":"600","rate":"11.51"},{"from":"601","to":null,"rate":"13.26"}]"""
                    db.execSQL(
                        "INSERT OR IGNORE INTO tariff_version (id, planId, effectiveFromCycle, slabs, fixedCharge, demandCharge, vatPercent, rebatePercent, otherCharges, createdAt) " +
                            "VALUES (1, 1, '1970-01', '$defaultSlabsJson', '10.00', '35.00', '5.0', '0.0', '0.0', $nowMillis)"
                    )
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
    fun provideTariffVersionDao(database: PowerUnitDatabase): TariffVersionDao = database.tariffVersionDao()

    @Provides
    fun provideBudgetDao(database: PowerUnitDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRechargeDao(database: PowerUnitDatabase): RechargeDao = database.rechargeDao()
}
