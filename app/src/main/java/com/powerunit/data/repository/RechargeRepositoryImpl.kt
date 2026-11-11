package com.powerunit.data.repository

import com.powerunit.core.database.dao.RechargeDao
import com.powerunit.core.database.entity.RechargeEntity
import com.powerunit.domain.repository.RechargeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RechargeRepositoryImpl @Inject constructor(
    private val rechargeDao: RechargeDao
) : RechargeRepository {

    override fun getRecharges(meterId: Long): Flow<List<RechargeEntity>> =
        rechargeDao.getRechargesByMeter(meterId)

    override suspend fun saveRecharge(recharge: RechargeEntity): Long =
        rechargeDao.insertRecharge(recharge)

    override suspend fun deleteRecharge(recharge: RechargeEntity) =
        rechargeDao.deleteRecharge(recharge)
}
