package com.powerunit.domain.repository

import com.powerunit.domain.model.Tariff
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

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
}
