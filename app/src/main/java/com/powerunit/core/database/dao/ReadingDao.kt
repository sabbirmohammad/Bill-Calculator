package com.powerunit.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.powerunit.core.database.entity.ReadingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY recordedAt DESC")
    fun getReadingsByMeter(meterId: Long): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE meterId = :meterId AND recordedAt >= :start AND recordedAt <= :end ORDER BY recordedAt ASC")
    fun getReadingsBetween(meterId: Long, start: Instant, end: Instant): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings WHERE meterId = :meterId AND recordedAt >= :start AND recordedAt <= :end ORDER BY recordedAt ASC")
    suspend fun getReadingsBetweenSync(meterId: Long, start: Instant, end: Instant): List<ReadingEntity>

    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY recordedAt DESC LIMIT 1")
    fun getLatestReading(meterId: Long): Flow<ReadingEntity?>

    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestReadingSync(meterId: Long): ReadingEntity?

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReadingById(id: Long): ReadingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<ReadingEntity>)

    @Update
    suspend fun updateReading(reading: ReadingEntity)

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)

    @Query("DELETE FROM readings")
    suspend fun deleteAllReadings()
}
