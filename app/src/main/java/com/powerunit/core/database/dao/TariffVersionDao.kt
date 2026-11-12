package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.powerunit.core.database.entity.TariffVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TariffVersionDao {

    @Query("SELECT * FROM tariff_version ORDER BY effectiveFromCycle DESC")
    fun getAllVersions(): Flow<List<TariffVersionEntity>>

    @Query("SELECT * FROM tariff_version ORDER BY effectiveFromCycle DESC")
    suspend fun getAllVersionsSync(): List<TariffVersionEntity>

    @Query("SELECT * FROM tariff_version WHERE planId = :planId AND effectiveFromCycle <= :cycle ORDER BY effectiveFromCycle DESC LIMIT 1")
    fun getVersionForCycle(planId: Long = 1L, cycle: String): Flow<TariffVersionEntity?>

    @Query("SELECT * FROM tariff_version WHERE planId = :planId AND effectiveFromCycle <= :cycle ORDER BY effectiveFromCycle DESC LIMIT 1")
    suspend fun getVersionForCycleSync(planId: Long = 1L, cycle: String): TariffVersionEntity?

    @Query("SELECT * FROM tariff_version WHERE planId = :planId ORDER BY effectiveFromCycle DESC LIMIT 1")
    fun getLatestVersion(planId: Long = 1L): Flow<TariffVersionEntity?>

    @Query("SELECT * FROM tariff_version WHERE planId = :planId ORDER BY effectiveFromCycle DESC LIMIT 1")
    suspend fun getLatestVersionSync(planId: Long = 1L): TariffVersionEntity?

    @Query("SELECT * FROM tariff_version WHERE id = :id")
    suspend fun getVersionById(id: Long): TariffVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: TariffVersionEntity): Long

    @Query("DELETE FROM tariff_version WHERE id = :id")
    suspend fun deleteVersionById(id: Long)

    @Query("SELECT COUNT(*) FROM tariff_version")
    suspend fun getCount(): Int

    @Query("DELETE FROM tariff_version")
    suspend fun deleteAllVersions()
}
