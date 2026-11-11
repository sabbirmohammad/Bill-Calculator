package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleStartDate: Instant,
    val unitLimit: BigDecimal? = null,
    val amountLimit: BigDecimal? = null
)
