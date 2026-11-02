package com.bangla.electricitybill.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bangla.electricitybill.domain.model.AdditionalChargeType
import com.bangla.electricitybill.domain.model.ChargeCalculationMethod
import com.bangla.electricitybill.domain.model.DataCompletenessStatus
import com.bangla.electricitybill.domain.model.MeterEventType
import com.bangla.electricitybill.domain.model.MeterStatus
import com.bangla.electricitybill.domain.model.PeriodStatus
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.TariffBillingMode
import com.bangla.electricitybill.domain.model.TariffCalculationMethod
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "meters")
data class MeterEntity(
    @PrimaryKey val id: String,
    val meterName: String,
    val meterNumber: String?,
    val meterType: String,
    val blackDigitCapacity: Int,
    val decimalPlaces: Int,
    val meterStatus: MeterStatus,
    val installedAt: LocalDateTime,
    val createdAt: LocalDateTime
)

@Entity(
    tableName = "meter_readings",
    foreignKeys = [
        ForeignKey(
            entity = MeterEntity::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meterId", "readingDateTime"], unique = true),
        Index(value = ["meterId"]),
        Index(value = ["readingDateTime"])
    ]
)
data class MeterReadingEntity(
    @PrimaryKey val id: String,
    val meterId: String,
    val blackDigits: String,
    val redDigit: String,
    val normalizedReading: BigDecimal,
    val readingDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isBaseline: Boolean,
    val note: String?,
    val correctionMetadata: String?,
    val readingSource: ReadingSource
)

@Entity(
    tableName = "billing_periods",
    foreignKeys = [
        ForeignKey(
            entity = MeterEntity::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meterId"]),
        Index(value = ["periodStart", "periodEnd"])
    ]
)
data class BillingPeriodEntity(
    @PrimaryKey val id: String,
    val meterId: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val openingReading: BigDecimal?,
    val closingReading: BigDecimal?,
    val tariffVersionId: String,
    val periodStatus: PeriodStatus,
    val trackingStartDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

@Entity(tableName = "tariffs")
data class TariffEntity(
    @PrimaryKey val id: String,
    val tariffName: String,
    val billingMode: TariffBillingMode,
    val calculationMethod: TariffCalculationMethod,
    val effectiveFrom: LocalDateTime,
    val effectiveUntil: LocalDateTime?,
    val currency: String,
    val isActive: Boolean,
    val flatRate: BigDecimal?
)

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
    @PrimaryKey val id: String,
    val tariffId: String,
    val minimumUnits: BigDecimal,
    val maximumUnits: BigDecimal?,
    val ratePerUnit: BigDecimal,
    val displayOrder: Int
)

@Entity(
    tableName = "additional_charges",
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
data class AdditionalChargeEntity(
    @PrimaryKey val id: String,
    val tariffId: String,
    val chargeType: AdditionalChargeType,
    val calculationMethod: ChargeCalculationMethod,
    val amount: BigDecimal,
    val isEnabled: Boolean,
    val vatApplicable: Boolean,
    val customName: String?
)

@Entity(
    tableName = "monthly_summaries",
    foreignKeys = [
        ForeignKey(
            entity = BillingPeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["billingPeriodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["billingPeriodId"], unique = true)
    ]
)
data class MonthlySummaryEntity(
    @PrimaryKey val id: String,
    val billingPeriodId: String,
    val totalConsumption: BigDecimal,
    val energyCost: BigDecimal,
    val additionalCharges: BigDecimal,
    val vatAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalEstimatedBill: BigDecimal,
    val calculationTimestamp: LocalDateTime,
    val dataCompletenessStatus: DataCompletenessStatus
)

@Entity(
    tableName = "meter_events",
    foreignKeys = [
        ForeignKey(
            entity = MeterEntity::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meterId"])
    ]
)
data class MeterEventEntity(
    @PrimaryKey val id: String,
    val meterId: String,
    val eventType: MeterEventType,
    val eventDate: LocalDateTime,
    val previousMeterId: String?,
    val replacementMeterId: String?,
    val description: String,
    val createdAt: LocalDateTime
)
