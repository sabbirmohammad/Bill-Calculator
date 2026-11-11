package com.powerunit.data.mapper

import com.powerunit.core.database.dao.TariffWithSlabs
import com.powerunit.core.database.entity.MeterEntity
import com.powerunit.core.database.entity.ReadingEntity
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity
import com.powerunit.domain.model.Meter
import com.powerunit.domain.model.MeterType
import com.powerunit.domain.model.Reading
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab

fun MeterEntity.toDomain(): Meter = Meter(
    id = id,
    name = name,
    type = if (type == "PREPAID") MeterType.PREPAID else MeterType.POSTPAID,
    startReading = startReading,
    multiplierFactor = multiplierFactor,
    isActive = isActive,
    createdAt = createdAt
)

fun Meter.toEntity(): MeterEntity = MeterEntity(
    id = id,
    name = name,
    type = type.name,
    startReading = startReading,
    multiplierFactor = multiplierFactor,
    isActive = isActive,
    createdAt = createdAt
)

fun ReadingEntity.toDomain(): Reading = Reading(
    id = id,
    meterId = meterId,
    readingValue = readingValue,
    recordedAt = recordedAt,
    note = note,
    photoUri = photoUri,
    isEstimated = isEstimated,
    isMeterReset = isMeterReset,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Reading.toEntity(): ReadingEntity = ReadingEntity(
    id = id,
    meterId = meterId,
    readingValue = readingValue,
    recordedAt = recordedAt,
    note = note,
    photoUri = photoUri,
    isEstimated = isEstimated,
    isMeterReset = isMeterReset,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TariffSlabEntity.toDomain(): TariffSlab = TariffSlab(
    id = id,
    tariffId = tariffId,
    fromUnit = fromUnit,
    toUnit = toUnit,
    ratePerUnit = ratePerUnit
)

fun TariffSlab.toEntity(tariffId: Long): TariffSlabEntity = TariffSlabEntity(
    id = id,
    tariffId = tariffId,
    fromUnit = fromUnit,
    toUnit = toUnit,
    ratePerUnit = ratePerUnit
)

fun TariffWithSlabs.toDomain(): Tariff = Tariff(
    id = tariff.id,
    name = tariff.name,
    effectiveFrom = tariff.effectiveFrom,
    vatPercent = tariff.vatPercent,
    serviceChargeFlat = tariff.serviceChargeFlat,
    demandChargeFlat = tariff.demandChargeFlat,
    isActive = tariff.isActive,
    slabs = slabs.map { it.toDomain() }.sortedBy { it.fromUnit }
)

fun Tariff.toEntity(): TariffEntity = TariffEntity(
    id = id,
    name = name,
    effectiveFrom = effectiveFrom,
    vatPercent = vatPercent,
    serviceChargeFlat = serviceChargeFlat,
    demandChargeFlat = demandChargeFlat,
    isActive = isActive
)
