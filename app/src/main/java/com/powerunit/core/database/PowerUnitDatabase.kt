package com.powerunit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.powerunit.core.database.converter.BigDecimalConverter
import com.powerunit.core.database.converter.InstantConverter
import com.powerunit.core.database.dao.BudgetDao
import com.powerunit.core.database.dao.MeterDao
import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.core.database.dao.RechargeDao
import com.powerunit.core.database.dao.TariffDao
import com.powerunit.core.database.dao.TariffVersionDao
import com.powerunit.core.database.entity.BudgetEntity
import com.powerunit.core.database.entity.MeterEntity
import com.powerunit.core.database.entity.ReadingEntity
import com.powerunit.core.database.entity.RechargeEntity
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity
import com.powerunit.core.database.entity.TariffVersionEntity
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Database(
    entities = [
        MeterEntity::class,
        ReadingEntity::class,
        TariffEntity::class,
        TariffSlabEntity::class,
        TariffVersionEntity::class,
        BudgetEntity::class,
        RechargeEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class, InstantConverter::class)
abstract class PowerUnitDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun readingDao(): ReadingDao
    abstract fun tariffDao(): TariffDao
    abstract fun tariffVersionDao(): TariffVersionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun rechargeDao(): RechargeDao

    companion object {
        const val DATABASE_NAME = "powerunit_secure.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tariff_version` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `planId` INTEGER NOT NULL,
                        `effectiveFromCycle` TEXT NOT NULL,
                        `slabs` TEXT NOT NULL,
                        `fixedCharge` TEXT NOT NULL,
                        `demandCharge` TEXT NOT NULL,
                        `vatPercent` TEXT NOT NULL,
                        `rebatePercent` TEXT NOT NULL,
                        `otherCharges` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tariff_version_planId_effectiveFromCycle` ON `tariff_version` (`planId`, `effectiveFromCycle`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tariff_version_effectiveFromCycle` ON `tariff_version` (`effectiveFromCycle`)")
            }
        }

        fun createDefaultBangladeshTariff(effectiveFrom: Instant): Pair<TariffEntity, List<TariffSlabEntity>> {
            val tariff = TariffEntity(
                name = "Bangladesh Residential LT-A (Default)",
                effectiveFrom = effectiveFrom,
                vatPercent = BigDecimal("5.0"),
                serviceChargeFlat = BigDecimal("10.00"),
                demandChargeFlat = BigDecimal("35.00"),
                isActive = true
            )

            val slabs = listOf(
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.35")),
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("201"), toUnit = BigDecimal("300"), ratePerUnit = BigDecimal("6.95")),
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("301"), toUnit = BigDecimal("400"), ratePerUnit = BigDecimal("7.34")),
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("401"), toUnit = BigDecimal("600"), ratePerUnit = BigDecimal("11.51")),
                TariffSlabEntity(tariffId = 0, fromUnit = BigDecimal("601"), toUnit = null, ratePerUnit = BigDecimal("13.26"))
            )

            return Pair(tariff, slabs)
        }
    }
}
