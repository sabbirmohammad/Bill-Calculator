package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "tariff_slabs",
    foreignKeys = [
        ForeignKey(
            entity = TariffEntity::class,
            parentColumns = ["id"],
            childColumns = ["tariffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tariffId"])
    ]
)
data class TariffSlabEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tariffId: Long,
    val fromUnit: BigDecimal,
    val toUnit: BigDecimal? = null, // null represents unlimited / above slab
    val ratePerUnit: BigDecimal
)
