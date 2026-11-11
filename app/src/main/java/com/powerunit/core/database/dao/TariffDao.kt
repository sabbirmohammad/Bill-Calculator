package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TariffDao {
    @Query("SELECT * FROM tariffs WHERE isActive = 1 ORDER BY effectiveFrom DESC LIMIT 1")
    fun getActiveTariff(): Flow<TariffEntity?>

    @Transaction
    @Query("SELECT * FROM tariffs WHERE isActive = 1 ORDER BY effectiveFrom DESC LIMIT 1")
    fun getActiveTariffWithSlabs(): Flow<TariffWithSlabs?>

    @Transaction
    @Query("SELECT * FROM tariffs WHERE isActive = 1 ORDER BY effectiveFrom DESC LIMIT 1")
    suspend fun getActiveTariffWithSlabsSync(): TariffWithSlabs?

    @Transaction
    @Query("SELECT * FROM tariffs WHERE effectiveFrom <= :date AND isActive = 1 ORDER BY effectiveFrom DESC LIMIT 1")
    suspend fun getTariffEffectiveAtSync(date: Instant): TariffWithSlabs?

    @Transaction
    @Query("SELECT * FROM tariffs WHERE id = :tariffId")
    suspend fun getTariffWithSlabsById(tariffId: Long): TariffWithSlabs?

    @Query("SELECT * FROM tariffs ORDER BY effectiveFrom DESC")
    fun getAllTariffs(): Flow<List<TariffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTariff(tariff: TariffEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlabs(slabs: List<TariffSlabEntity>)

    @Update
    suspend fun updateTariff(tariff: TariffEntity)

    @Query("DELETE FROM tariff_slabs WHERE tariffId = :tariffId")
    suspend fun deleteSlabsByTariffId(tariffId: Long)

    @Transaction
    suspend fun updateTariffAndSlabs(tariff: TariffEntity, slabs: List<TariffSlabEntity>) {
        updateTariff(tariff)
        deleteSlabsByTariffId(tariff.id)
        insertSlabs(slabs.map { it.copy(tariffId = tariff.id) })
    }
}
