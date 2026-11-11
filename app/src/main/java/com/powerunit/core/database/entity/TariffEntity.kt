package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Entity(tableName = "tariffs")
data class TariffEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val effectiveFrom: Instant,
    val vatPercent: BigDecimal,
    val serviceChargeFlat: BigDecimal,
    val demandChargeFlat: BigDecimal,
    val isActive: Boolean = true
)
