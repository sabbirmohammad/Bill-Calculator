package com.bangla.electricitybill.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class PeriodStatus {
    IN_PROGRESS,
    COMPLETED,
    PARTIAL_TRACKING,
    INCOMPLETE_BOUNDARY_DATA,
    ESTIMATED
}

enum class DataCompletenessStatus {
    COMPLETE,
    PARTIAL,
    INCOMPLETE_BOUNDARY,
    ESTIMATED
}

enum class MeterEventType {
    INITIALIZATION,
    COUNTER_ROLLOVER,
    METER_REPLACED,
    TARIFF_CHANGED,
    HISTORICAL_CORRECTION
}

data class BillingPeriod(
    val id: String,
    val meterId: String,
    val periodStart: LocalDateTime,
    val periodEnd: LocalDateTime,
    val openingReading: BigDecimal? = null,
    val closingReading: BigDecimal? = null,
    val tariffVersionId: String,
    val periodStatus: PeriodStatus = PeriodStatus.IN_PROGRESS,
    val trackingStartDate: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime? = null
)

data class MonthlySummary(
    val id: String,
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

data class MeterEvent(
    val id: String,
    val meterId: String,
    val eventType: MeterEventType,
    val eventDate: LocalDateTime,
    val previousMeterId: String? = null,
    val replacementMeterId: String? = null,
    val description: String,
    val createdAt: LocalDateTime
)

/**
 * Result of reading interval calculation
 */
data class IntervalConsumptionResult(
    val previousReading: MeterReading,
    val currentReading: MeterReading,
    val consumptionUnits: BigDecimal,
    val elapsedDays: Long,
    val averageDailyConsumption: BigDecimal,
    val intervalEnergyCost: BigDecimal,
    val isConsecutiveDaily: Boolean,
    val label: String
)

/**
 * Result of monthly forecast
 */
data class ForecastResult(
    val currentConsumption: BigDecimal,
    val elapsedDays: Long,
    val remainingDays: Long,
    val averageDailyConsumption: BigDecimal,
    val projectedAdditionalConsumption: BigDecimal,
    val projectedTotalConsumption: BigDecimal,
    val projectedEnergyCost: BigDecimal,
    val projectedTotalBill: BigDecimal,
    val budgetAmount: BigDecimal? = null,
    val budgetExceeded: Boolean = false
)

/**
 * Detailed breakdown of progressive slab charges
 */
data class SlabChargeBreakdown(
    val slab: TariffSlab,
    val unitsInSlab: BigDecimal,
    val rate: BigDecimal,
    val cost: BigDecimal
)

/**
 * Complete itemized bill breakdown
 */
data class ItemizedBill(
    val totalUnits: BigDecimal,
    val slabBreakdowns: List<SlabChargeBreakdown>,
    val energyCost: BigDecimal,
    val demandCharge: BigDecimal,
    val meterRent: BigDecimal,
    val serviceCharge: BigDecimal,
    val otherCharges: BigDecimal,
    val vatAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val totalBill: BigDecimal
)
