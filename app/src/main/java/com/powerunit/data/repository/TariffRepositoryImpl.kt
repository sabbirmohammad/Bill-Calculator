package com.powerunit.data.repository

import androidx.room.withTransaction
import com.powerunit.core.common.DateUtils
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.dao.TariffDao
import com.powerunit.core.database.dao.TariffVersionDao
import com.powerunit.core.database.entity.TariffEntity
import com.powerunit.core.database.entity.TariffSlabEntity
import com.powerunit.core.database.entity.TariffVersionEntity
import com.powerunit.core.datastore.SettingsPreferences
import com.powerunit.data.mapper.toDomain
import com.powerunit.data.mapper.toEntity
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffSlab
import com.powerunit.domain.model.TariffVersion
import com.powerunit.domain.repository.TariffRecalculationResult
import com.powerunit.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TariffRepositoryImpl @Inject constructor(
    private val database: PowerUnitDatabase,
    private val tariffDao: TariffDao,
    private val tariffVersionDao: TariffVersionDao,
    private val settingsPreferences: SettingsPreferences
) : TariffRepository {

    override fun getActiveTariff(): Flow<Tariff?> {
        return tariffDao.getActiveTariffWithSlabs().map { it?.toDomain() }
    }

    override suspend fun getActiveTariffSync(): Tariff? {
        return tariffDao.getActiveTariffWithSlabsSync()?.toDomain()
    }

    override suspend fun getTariffEffectiveAt(date: Instant): Tariff? {
        val cycle = DateUtils.instantToCycleString(date)
        val version = tariffVersionDao.getVersionForCycleSync(cycle = cycle)
        return if (version != null) {
            version.toTariff()
        } else {
            tariffDao.getTariffEffectiveAtSync(date)?.toDomain()
        }
    }

    override suspend fun getTariffById(id: Long): Tariff? =
        tariffDao.getTariffWithSlabsById(id)?.toDomain()

    override fun getAllTariffs(): Flow<List<Tariff>> =
        tariffDao.getAllTariffs().map { list ->
            list.map {
                val full = tariffDao.getTariffWithSlabsById(it.id)
                full?.toDomain() ?: Tariff(
                    id = it.id,
                    name = it.name,
                    effectiveFrom = it.effectiveFrom,
                    vatPercent = it.vatPercent,
                    serviceChargeFlat = it.serviceChargeFlat,
                    demandChargeFlat = it.demandChargeFlat,
                    isActive = it.isActive
                )
            }
        }

    override fun getAllTariffsWithSlabs(): Flow<List<Tariff>> =
        tariffDao.getAllTariffsWithSlabs().map { list -> list.map { it.toDomain() } }

    override suspend fun saveTariff(tariff: Tariff): Long {
        val tariffId = tariffDao.insertTariff(tariff.toEntity())
        tariffDao.insertSlabs(tariff.slabs.map { it.toEntity(tariffId) })
        return tariffId
    }

    override suspend fun updateTariff(tariff: Tariff) {
        tariffDao.updateTariffAndSlabs(tariff.toEntity(), tariff.slabs.map { it.toEntity(tariff.id) })
    }

    override suspend fun saveNewTariffVersion(tariff: Tariff): Long {
        return tariffDao.saveNewTariffVersion(
            tariff.toEntity(),
            tariff.slabs.map { it.toEntity(0) }
        )
    }

    override fun getAllTariffVersions(): Flow<List<TariffVersion>> {
        return tariffVersionDao.getAllVersions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTariffVersionForCycle(cycle: String, planId: Long): TariffVersion? {
        return tariffVersionDao.getVersionForCycleSync(planId = planId, cycle = cycle)?.toDomain()
    }

    override suspend fun getLatestTariffVersion(planId: Long): TariffVersion? {
        return tariffVersionDao.getLatestVersionSync(planId = planId)?.toDomain()
    }

    override suspend fun saveTariffVersionAndRecalculate(newVersion: TariffVersion, planName: String?): TariffRecalculationResult {
        return database.withTransaction {
            val previousLatest = tariffVersionDao.getLatestVersionSync(newVersion.planId)

            // 1. Insert new version
            val versionEntity = TariffVersionEntity.fromDomain(newVersion)
            val newVersionId = tariffVersionDao.insertVersion(versionEntity)

            // 2. Synchronize active tariffs & tariff_slabs table so legacy / reactive flows reflect updates
            val effectiveInstant = DateUtils.cycleStringToInstant(newVersion.effectiveFromCycle)
            val resolvedName = if (!planName.isNullOrBlank()) planName else "Tariff (${newVersion.effectiveFromCycle})"
            val tariffEntity = TariffEntity(
                id = 1,
                name = resolvedName,
                effectiveFrom = effectiveInstant,
                vatPercent = newVersion.vatPercent,
                serviceChargeFlat = newVersion.fixedCharge,
                demandChargeFlat = newVersion.demandCharge,
                isActive = true
            )
            tariffDao.updateTariffAndSlabs(
                tariffEntity,
                newVersion.slabs.map { slab ->
                    TariffSlabEntity(
                        tariffId = 1,
                        fromUnit = slab.fromUnit,
                        toUnit = slab.toUnit,
                        ratePerUnit = slab.ratePerUnit
                    )
                }
            )

            // 3. Count recalculated months from effectiveFromCycle to currentCycle
            val currentCycle = DateUtils.currentCycleString()
            val monthsCount = computeMonthsCount(newVersion.effectiveFromCycle, currentCycle)

            TariffRecalculationResult(
                versionId = newVersionId,
                effectiveFromCycle = newVersion.effectiveFromCycle,
                monthsRecalculated = monthsCount,
                previousVersionId = previousLatest?.id
            )
        }
    }

    override suspend fun revertTariffVersion(versionId: Long): Boolean {
        return database.withTransaction {
            tariffVersionDao.deleteVersionById(versionId)
            val remainingLatest = tariffVersionDao.getLatestVersionSync(1L)
            if (remainingLatest != null) {
                val effectiveInstant = DateUtils.cycleStringToInstant(remainingLatest.effectiveFromCycle)
                val tariffEntity = TariffEntity(
                    id = 1,
                    name = "Tariff (${remainingLatest.effectiveFromCycle})",
                    effectiveFrom = effectiveInstant,
                    vatPercent = remainingLatest.vatPercent,
                    serviceChargeFlat = remainingLatest.fixedCharge,
                    demandChargeFlat = remainingLatest.demandCharge,
                    isActive = true
                )
                val slabs = TariffVersionEntity.deserializeSlabs(remainingLatest.slabs)
                tariffDao.updateTariffAndSlabs(
                    tariffEntity,
                    slabs.map { slab ->
                        TariffSlabEntity(
                            tariffId = 1,
                            fromUnit = slab.fromUnit,
                            toUnit = slab.toUnit,
                            ratePerUnit = slab.ratePerUnit
                        )
                    }
                )
            }
            true
        }
    }

    override suspend fun seedDefaultsIfNeeded() {
        val isSeeded = settingsPreferences.isTariffSeeded()
        val count = tariffVersionDao.getCount()
        if (!isSeeded && count == 0) {
            val defaultSlabs = listOf(
                TariffSlab(fromUnit = BigDecimal("0"), toUnit = BigDecimal("75"), ratePerUnit = BigDecimal("4.85")),
                TariffSlab(fromUnit = BigDecimal("76"), toUnit = BigDecimal("200"), ratePerUnit = BigDecimal("6.63")),
                TariffSlab(fromUnit = BigDecimal("201"), toUnit = BigDecimal("300"), ratePerUnit = BigDecimal("6.95")),
                TariffSlab(fromUnit = BigDecimal("301"), toUnit = BigDecimal("400"), ratePerUnit = BigDecimal("7.34")),
                TariffSlab(fromUnit = BigDecimal("401"), toUnit = BigDecimal("600"), ratePerUnit = BigDecimal("11.51")),
                TariffSlab(fromUnit = BigDecimal("601"), toUnit = null, ratePerUnit = BigDecimal("13.26"))
            )
            val defaultVersion = TariffVersion(
                id = 1L,
                planId = 1L,
                effectiveFromCycle = "1970-01",
                slabs = defaultSlabs,
                fixedCharge = BigDecimal("10.00"),
                demandCharge = BigDecimal("35.00"),
                vatPercent = BigDecimal("5.0"),
                rebatePercent = BigDecimal.ZERO,
                otherCharges = BigDecimal.ZERO
            )
            tariffVersionDao.insertVersion(TariffVersionEntity.fromDomain(defaultVersion))
            settingsPreferences.setTariffSeeded(true)
        }
    }

    private fun computeMonthsCount(startCycle: String, endCycle: String): Int {
        val startParts = startCycle.split("-")
        val endParts = endCycle.split("-")
        val startYear = startParts.getOrNull(0)?.toIntOrNull() ?: 2026
        val startMonth = startParts.getOrNull(1)?.toIntOrNull() ?: 1
        val endYear = endParts.getOrNull(0)?.toIntOrNull() ?: 2026
        val endMonth = endParts.getOrNull(1)?.toIntOrNull() ?: 1

        val total = (endYear - startYear) * 12 + (endMonth - startMonth) + 1
        return total.coerceAtLeast(1)
    }
}
