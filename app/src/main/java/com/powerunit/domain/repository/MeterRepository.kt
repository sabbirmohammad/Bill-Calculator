package com.powerunit.domain.repository

import com.powerunit.domain.model.Meter
import kotlinx.coroutines.flow.Flow

interface MeterRepository {
    fun getAllMeters(): Flow<List<Meter>>
    fun getActiveMeter(): Flow<Meter?>
    suspend fun getActiveMeterSync(): Meter?
    suspend fun getMeterById(id: Long): Meter?
    suspend fun saveMeter(meter: Meter): Long
    suspend fun updateMeter(meter: Meter)
    suspend fun deleteMeter(meter: Meter)
}
