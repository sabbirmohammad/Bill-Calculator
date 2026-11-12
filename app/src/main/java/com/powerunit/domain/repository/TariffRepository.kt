package com.powerunit.domain.repository

import com.powerunit.domain.model.Tariff
import com.powerunit.domain.model.TariffVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

data class TariffRecalculationResult(
    val versionId: Long,
    val effectiveFromCycle: String,
    val monthsRecalculated: Int,
    val previousVersionId: Long?
)

interface TariffRepository {
    fun getActiveTariff(): Flow<Tariff?>
    suspend fun getActiveTariffSync(): Tariff?
    suspend fun getTariffEffectiveAt(date: Instant): Tariff?
    suspend fun getTariffById(id: Long): Tariff?
    fun getAllTariffs(): Flow<List<Tariff>>
    fun getAllTariffsWithSlabs(): Flow<List<Tariff>>
    suspend fun saveTariff(tariff: Tariff): Long
    suspend fun updateTariff(tariff: Tariff)
    suspend fun saveNewTariffVersion(tariff: Tariff): Long

    // Tariff Versioning and Month Recalculation
    fun getAllTariffVersions(): Flow<List<TariffVersion>>
    suspend fun getTariffVersionForCycle(cycle: String, planId: Long = 1L): TariffVersion?
    suspend fun getLatestTariffVersion(planId: Long = 1L): TariffVersion?
    suspend fun saveTariffVersionAndRecalculate(newVersion: TariffVersion, planName: String? = null): TariffRecalculationResult
    suspend fun revertTariffVersion(versionId: Long): Boolean
    suspend fun seedDefaultsIfNeeded()
}
