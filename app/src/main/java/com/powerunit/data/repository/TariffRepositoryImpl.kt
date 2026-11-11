package com.powerunit.data.repository

import com.powerunit.core.database.dao.TariffDao
import com.powerunit.data.mapper.toDomain
import com.powerunit.data.mapper.toEntity
import com.powerunit.domain.model.Tariff
import com.powerunit.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TariffRepositoryImpl @Inject constructor(
    private val tariffDao: TariffDao
) : TariffRepository {

    override fun getActiveTariff(): Flow<Tariff?> =
        tariffDao.getActiveTariffWithSlabs().map { it?.toDomain() }

    override suspend fun getActiveTariffSync(): Tariff? =
        tariffDao.getActiveTariffWithSlabsSync()?.toDomain()

    override suspend fun getTariffEffectiveAt(date: Instant): Tariff? =
        tariffDao.getTariffEffectiveAtSync(date)?.toDomain()

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

    override suspend fun saveTariff(tariff: Tariff): Long {
        val tariffId = tariffDao.insertTariff(tariff.toEntity())
        tariffDao.insertSlabs(tariff.slabs.map { it.toEntity(tariffId) })
        return tariffId
    }

    override suspend fun updateTariff(tariff: Tariff) {
        tariffDao.updateTariffAndSlabs(tariff.toEntity(), tariff.slabs.map { it.toEntity(tariff.id) })
    }
}
