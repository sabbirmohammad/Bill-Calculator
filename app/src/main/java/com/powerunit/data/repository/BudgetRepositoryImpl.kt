package com.powerunit.data.repository

import com.powerunit.core.database.dao.BudgetDao
import com.powerunit.core.database.entity.BudgetEntity
import com.powerunit.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getLatestBudget(): Flow<BudgetEntity?> =
        budgetDao.getLatestBudget()

    override suspend fun getLatestBudgetSync(): BudgetEntity? =
        budgetDao.getLatestBudgetSync()

    override suspend fun getBudgetForCycle(startDate: Instant): BudgetEntity? =
        budgetDao.getBudgetForCycle(startDate)

    override suspend fun saveBudget(budget: BudgetEntity): Long =
        budgetDao.insertOrUpdateBudget(budget)

    override fun getAllBudgets(): Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgets()
}
