package com.powerunit.data.repository

import com.powerunit.core.database.dao.MeterDao
import com.powerunit.data.mapper.toDomain
import com.powerunit.data.mapper.toEntity
import com.powerunit.domain.model.Meter
import com.powerunit.domain.repository.MeterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeterRepositoryImpl @Inject constructor(
    private val meterDao: MeterDao
) : MeterRepository {

    override fun getAllMeters(): Flow<List<Meter>> =
        meterDao.getAllMeters().map { list -> list.map { it.toDomain() } }

    override fun getActiveMeter(): Flow<Meter?> =
        meterDao.getActiveMeter().map { it?.toDomain() }

    override suspend fun getActiveMeterSync(): Meter? {
        val existing = meterDao.getActiveMeterSync()?.toDomain()
        if (existing != null) return existing
        val defaultMeter = Meter(
            name = "Main Meter",
            type = com.powerunit.domain.model.MeterType.POSTPAID,
            startReading = java.math.BigDecimal.ZERO,
            multiplierFactor = java.math.BigDecimal.ONE,
            isActive = true,
            createdAt = com.powerunit.core.common.DateUtils.currentInstant()
        )
        val id = meterDao.insertMeter(defaultMeter.toEntity())
        return defaultMeter.copy(id = id)
    }

    override suspend fun getMeterById(id: Long): Meter? =
        meterDao.getMeterById(id)?.toDomain()

    override suspend fun saveMeter(meter: Meter): Long =
        meterDao.insertMeter(meter.toEntity())

    override suspend fun updateMeter(meter: Meter) =
        meterDao.updateMeter(meter.toEntity())

    override suspend fun deleteMeter(meter: Meter) =
        meterDao.deleteMeter(meter.toEntity())
}
