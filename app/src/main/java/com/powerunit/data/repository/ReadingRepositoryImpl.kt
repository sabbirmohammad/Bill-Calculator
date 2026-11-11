package com.powerunit.data.repository

import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.data.mapper.toDomain
import com.powerunit.data.mapper.toEntity
import com.powerunit.domain.model.Reading
import com.powerunit.domain.repository.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val readingDao: ReadingDao
) : ReadingRepository {

    override fun getReadings(meterId: Long): Flow<List<Reading>> =
        readingDao.getReadingsByMeter(meterId).map { list -> list.map { it.toDomain() } }

    override fun getReadingsBetween(meterId: Long, start: Instant, end: Instant): Flow<List<Reading>> =
        readingDao.getReadingsBetween(meterId, start, end).map { list -> list.map { it.toDomain() } }

    override suspend fun getReadingsBetweenSync(meterId: Long, start: Instant, end: Instant): List<Reading> =
        readingDao.getReadingsBetweenSync(meterId, start, end).map { it.toDomain() }

    override fun getLatestReading(meterId: Long): Flow<Reading?> =
        readingDao.getLatestReading(meterId).map { it?.toDomain() }

    override suspend fun getLatestReadingSync(meterId: Long): Reading? =
        readingDao.getLatestReadingSync(meterId)?.toDomain()

    override suspend fun getReadingById(id: Long): Reading? =
        readingDao.getReadingById(id)?.toDomain()

    override suspend fun saveReading(reading: Reading): Long =
        readingDao.insertReading(reading.toEntity())

    override suspend fun saveReadings(readings: List<Reading>) =
        readingDao.insertReadings(readings.map { it.toEntity() })

    override suspend fun updateReading(reading: Reading) =
        readingDao.updateReading(reading.toEntity())

    override suspend fun deleteReading(id: Long) =
        readingDao.deleteReadingById(id)
}
