package com.powerunit.domain.repository

import com.powerunit.core.database.entity.RechargeEntity
import kotlinx.coroutines.flow.Flow

interface RechargeRepository {
    fun getRecharges(meterId: Long): Flow<List<RechargeEntity>>
    suspend fun saveRecharge(recharge: RechargeEntity): Long
    suspend fun deleteRecharge(recharge: RechargeEntity)
}
