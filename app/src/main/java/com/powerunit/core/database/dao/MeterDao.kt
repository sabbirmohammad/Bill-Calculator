package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.powerunit.core.database.entity.MeterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    @Query("SELECT * FROM meters ORDER BY createdAt ASC")
    fun getAllMeters(): Flow<List<MeterEntity>>

    @Query("SELECT * FROM meters WHERE isActive = 1 LIMIT 1")
    fun getActiveMeter(): Flow<MeterEntity?>

    @Query("SELECT * FROM meters WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveMeterSync(): MeterEntity?

    @Query("SELECT * FROM meters WHERE id = :id")
    suspend fun getMeterById(id: Long): MeterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeter(meter: MeterEntity): Long

    @Update
    suspend fun updateMeter(meter: MeterEntity)

    @Delete
    suspend fun deleteMeter(meter: MeterEntity)
}
