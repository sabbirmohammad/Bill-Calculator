package com.powerunit.domain.repository

import com.powerunit.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface BudgetRepository {
    fun getLatestBudget(): Flow<BudgetEntity?>
    suspend fun getLatestBudgetSync(): BudgetEntity?
    suspend fun getBudgetForCycle(startDate: Instant): BudgetEntity?
    suspend fun saveBudget(budget: BudgetEntity): Long
    fun getAllBudgets(): Flow<List<BudgetEntity>>
}
