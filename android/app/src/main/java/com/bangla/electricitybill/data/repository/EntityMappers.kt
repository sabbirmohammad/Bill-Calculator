package com.bangla.electricitybill.data.repository

import com.bangla.electricitybill.data.entity.AdditionalChargeEntity
import com.bangla.electricitybill.data.entity.BillingPeriodEntity
import com.bangla.electricitybill.data.entity.MeterEntity
import com.bangla.electricitybill.data.entity.MeterEventEntity
import com.bangla.electricitybill.data.entity.MeterReadingEntity
import com.bangla.electricitybill.data.entity.MonthlySummaryEntity
import com.bangla.electricitybill.data.entity.TariffEntity
import com.bangla.electricitybill.data.entity.TariffSlabEntity
import com.bangla.electricitybill.domain.model.AdditionalCharge
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterEvent
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.model.TariffSlab

fun MeterEntity.toDomain(): Meter = Meter(
    id = id,
    meterName = meterName,
    meterNumber = meterNumber,
    meterType = meterType,
    blackDigitCapacity = blackDigitCapacity,
    decimalPlaces = decimalPlaces,
    meterStatus = meterStatus,
    installedAt = installedAt,
    createdAt = createdAt
)

fun Meter.toEntity(): MeterEntity = MeterEntity(
    id = id,
    meterName = meterName,
    meterNumber = meterNumber,
    meterType = meterType,
    blackDigitCapacity = blackDigitCapacity,
    decimalPlaces = decimalPlaces,
    meterStatus = meterStatus,
    installedAt = installedAt,
    createdAt = createdAt
)

fun MeterReadingEntity.toDomain(): MeterReading = MeterReading(
    id = id,
    meterId = meterId,
    blackDigits = blackDigits,
    redDigit = redDigit,
    normalizedReading = normalizedReading,
    readingDateTime = readingDateTime,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isBaseline = isBaseline,
    note = note,
    correctionMetadata = correctionMetadata,
    readingSource = readingSource
)

fun MeterReading.toEntity(): MeterReadingEntity = MeterReadingEntity(
    id = id,
    meterId = meterId,
    blackDigits = blackDigits,
    redDigit = redDigit,
    normalizedReading = normalizedReading,
    readingDateTime = readingDateTime,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isBaseline = isBaseline,
    note = note,
    correctionMetadata = correctionMetadata,
    readingSource = readingSource
)

fun BillingPeriodEntity.toDomain(): BillingPeriod = BillingPeriod(
    id = id,
    meterId = meterId,
    periodStart = periodStart,
    periodEnd = periodEnd,
    openingReading = openingReading,
    closingReading = closingReading,
    tariffVersionId = tariffVersionId,
    periodStatus = periodStatus,
    trackingStartDate = trackingStartDate,
    createdAt = createdAt,
    completedAt = completedAt
)

fun BillingPeriod.toEntity(): BillingPeriodEntity = BillingPeriodEntity(
    id = id,
    meterId = meterId,
    periodStart = periodStart,
    periodEnd = periodEnd,
    openingReading = openingReading,
    closingReading = closingReading,
    tariffVersionId = tariffVersionId,
    periodStatus = periodStatus,
    trackingStartDate = trackingStartDate,
    createdAt = createdAt,
    completedAt = completedAt
)

fun TariffEntity.toDomain(slabs: List<TariffSlabEntity>, charges: List<AdditionalChargeEntity>): Tariff = Tariff(
    id = id,
    tariffName = tariffName,
    billingMode = billingMode,
    calculationMethod = calculationMethod,
    effectiveFrom = effectiveFrom,
    effectiveUntil = effectiveUntil,
    currency = currency,
    isActive = isActive,
    flatRate = flatRate,
    slabs = slabs.map { it.toDomain() },
    additionalCharges = charges.map { it.toDomain() }
)

fun Tariff.toEntity(): TariffEntity = TariffEntity(
    id = id,
    tariffName = tariffName,
    billingMode = billingMode,
    calculationMethod = calculationMethod,
    effectiveFrom = effectiveFrom,
    effectiveUntil = effectiveUntil,
    currency = currency,
    isActive = isActive,
    flatRate = flatRate
)

fun TariffSlabEntity.toDomain(): TariffSlab = TariffSlab(
    id = id,
    tariffId = tariffId,
    minimumUnits = minimumUnits,
    maximumUnits = maximumUnits,
    ratePerUnit = ratePerUnit,
    displayOrder = displayOrder
)

fun TariffSlab.toEntity(): TariffSlabEntity = TariffSlabEntity(
    id = id,
    tariffId = tariffId,
    minimumUnits = minimumUnits,
    maximumUnits = maximumUnits,
    ratePerUnit = ratePerUnit,
    displayOrder = displayOrder
)

fun AdditionalChargeEntity.toDomain(): AdditionalCharge = AdditionalCharge(
    id = id,
    tariffId = tariffId,
    chargeType = chargeType,
    calculationMethod = calculationMethod,
    amount = amount,
    isEnabled = isEnabled,
    vatApplicable = vatApplicable,
    customName = customName
)

fun AdditionalCharge.toEntity(): AdditionalChargeEntity = AdditionalChargeEntity(
    id = id,
    tariffId = tariffId,
    chargeType = chargeType,
    calculationMethod = calculationMethod,
    amount = amount,
    isEnabled = isEnabled,
    vatApplicable = vatApplicable,
    customName = customName
)

fun MonthlySummaryEntity.toDomain(): MonthlySummary = MonthlySummary(
    id = id,
    billingPeriodId = billingPeriodId,
    totalConsumption = totalConsumption,
    energyCost = energyCost,
    additionalCharges = additionalCharges,
    vatAmount = vatAmount,
    discountAmount = discountAmount,
    totalEstimatedBill = totalEstimatedBill,
    calculationTimestamp = calculationTimestamp,
    dataCompletenessStatus = dataCompletenessStatus
)

fun MonthlySummary.toEntity(): MonthlySummaryEntity = MonthlySummaryEntity(
    id = id,
    billingPeriodId = billingPeriodId,
    totalConsumption = totalConsumption,
    energyCost = energyCost,
    additionalCharges = additionalCharges,
    vatAmount = vatAmount,
    discountAmount = discountAmount,
    totalEstimatedBill = totalEstimatedBill,
    calculationTimestamp = calculationTimestamp,
    dataCompletenessStatus = dataCompletenessStatus
)

fun MeterEventEntity.toDomain(): MeterEvent = MeterEvent(
    id = id,
    meterId = meterId,
    eventType = eventType,
    eventDate = eventDate,
    previousMeterId = previousMeterId,
    replacementMeterId = replacementMeterId,
    description = description,
    createdAt = createdAt
)

fun MeterEvent.toEntity(): MeterEventEntity = MeterEventEntity(
    id = id,
    meterId = meterId,
    eventType = eventType,
    eventDate = eventDate,
    previousMeterId = previousMeterId,
    replacementMeterId = replacementMeterId,
    description = description,
    createdAt = createdAt
)
