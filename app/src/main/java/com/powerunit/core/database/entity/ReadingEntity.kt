package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.math.BigDecimal

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = MeterEntity::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meterId", "recordedAt"], unique = true),
        Index(value = ["meterId"])
    ]
)
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,
    val readingValue: BigDecimal,
    val recordedAt: Instant,
    val note: String? = null,
    val photoUri: String? = null,
    val isEstimated: Boolean = false,
    val isMeterReset: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
