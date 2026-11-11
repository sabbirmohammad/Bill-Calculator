package com.powerunit.domain.repository

import com.powerunit.domain.model.Reading
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ReadingRepository {
    fun getReadings(meterId: Long): Flow<List<Reading>>
    fun getReadingsBetween(meterId: Long, start: Instant, end: Instant): Flow<List<Reading>>
    suspend fun getReadingsBetweenSync(meterId: Long, start: Instant, end: Instant): List<Reading>
    fun getLatestReading(meterId: Long): Flow<Reading?>
    suspend fun getLatestReadingSync(meterId: Long): Reading?
    suspend fun getReadingById(id: Long): Reading?
    suspend fun saveReading(reading: Reading): Long
    suspend fun saveReadings(readings: List<Reading>)
    suspend fun updateReading(reading: Reading)
    suspend fun deleteReading(id: Long)
}
