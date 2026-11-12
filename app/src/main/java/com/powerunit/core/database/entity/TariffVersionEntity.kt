package com.powerunit.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.powerunit.core.common.DateUtils
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.model.TariffVersion
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

@Serializable
data class TariffSlabDto(
    val from: String,
    val to: String? = null,
    val rate: String
)

@Entity(
    tableName = "tariff_version",
    indices = [
        Index(value = ["planId", "effectiveFromCycle"], unique = true),
        Index(value = ["effectiveFromCycle"])
    ]
)
data class TariffVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long = 1L,
    val effectiveFromCycle: String, // "YYYY-MM", e.g. "2026-11"
    val slabs: String, // JSON serialization of List<TariffSlabDto>
    val fixedCharge: BigDecimal,
    val demandCharge: BigDecimal,
    val vatPercent: BigDecimal,
    val rebatePercent: BigDecimal = BigDecimal.ZERO,
    val otherCharges: BigDecimal = BigDecimal.ZERO,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): TariffVersion {
        return TariffVersion(
            id = id,
            planId = planId,
            effectiveFromCycle = effectiveFromCycle,
            slabs = deserializeSlabs(slabs),
            fixedCharge = fixedCharge,
            demandCharge = demandCharge,
            vatPercent = vatPercent,
            rebatePercent = rebatePercent,
            otherCharges = otherCharges,
            createdAt = createdAt
        )
    }

    fun toTariff(): Tariff {
        val instant = DateUtils.cycleStringToInstant(effectiveFromCycle)
        return Tariff(
            id = id,
            name = "Tariff ($effectiveFromCycle)",
            effectiveFrom = instant,
            vatPercent = vatPercent,
            serviceChargeFlat = fixedCharge,
            demandChargeFlat = demandCharge,
            slabs = deserializeSlabs(slabs),
            isActive = true,
            rebatePercent = rebatePercent,
            otherCharges = otherCharges,
            effectiveFromCycle = effectiveFromCycle
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun serializeSlabs(slabs: List<TariffSlab>): String {
            val dtos = slabs.map {
                TariffSlabDto(
                    from = it.fromUnit.toPlainString(),
                    to = it.toUnit?.toPlainString(),
                    rate = it.ratePerUnit.toPlainString()
                )
            }
            return json.encodeToString(dtos)
        }

        fun deserializeSlabs(jsonStr: String): List<TariffSlab> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val dtos = json.decodeFromString<List<TariffSlabDto>>(jsonStr)
                dtos.map {
                    TariffSlab(
                        fromUnit = BigDecimal(it.from),
                        toUnit = it.to?.let { toVal -> BigDecimal(toVal) },
                        ratePerUnit = BigDecimal(it.rate)
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun fromDomain(version: TariffVersion): TariffVersionEntity {
            return TariffVersionEntity(
                id = version.id,
                planId = version.planId,
                effectiveFromCycle = version.effectiveFromCycle,
                slabs = serializeSlabs(version.slabs),
                fixedCharge = version.fixedCharge,
                demandCharge = version.demandCharge,
                vatPercent = version.vatPercent,
                rebatePercent = version.rebatePercent,
                otherCharges = version.otherCharges,
                createdAt = version.createdAt
            )
        }
    }
}
