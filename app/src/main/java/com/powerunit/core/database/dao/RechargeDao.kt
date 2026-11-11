package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.powerunit.core.database.entity.RechargeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RechargeDao {
    @Query("SELECT * FROM recharges WHERE meterId = :meterId ORDER BY date DESC")
    fun getRechargesByMeter(meterId: Long): Flow<List<RechargeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecharge(recharge: RechargeEntity): Long

    @Delete
    suspend fun deleteRecharge(recharge: RechargeEntity)
}
