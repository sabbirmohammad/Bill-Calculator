package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.powerunit.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY cycleStartDate DESC LIMIT 1")
    fun getLatestBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets ORDER BY cycleStartDate DESC LIMIT 1")
    suspend fun getLatestBudgetSync(): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE cycleStartDate = :startDate LIMIT 1")
    suspend fun getBudgetForCycle(startDate: Instant): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Query("SELECT * FROM budgets ORDER BY cycleStartDate DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
