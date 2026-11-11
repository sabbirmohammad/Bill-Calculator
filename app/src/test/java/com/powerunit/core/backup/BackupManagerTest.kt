package com.powerunit.core.backup

import android.content.Context
import com.powerunit.core.database.PowerUnitDatabase
import com.powerunit.core.database.dao.MeterDao
import com.powerunit.core.database.dao.ReadingDao
import com.powerunit.core.database.entity.MeterEntity
import com.powerunit.core.database.entity.ReadingEntity
import com.powerunit.domain.model.MeterType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.math.BigDecimal

class BackupManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>(relaxed = true)
    private val database = mockk<PowerUnitDatabase>(relaxed = true)
    private val meterDao = mockk<MeterDao>(relaxed = true)
    private val readingDao = mockk<ReadingDao>(relaxed = true)

    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        every { context.filesDir } returns tempFolder.root
        every { database.meterDao() } returns meterDao
        every { database.readingDao() } returns readingDao

        val sampleMeters = listOf(
            MeterEntity(
                id = 1L,
                name = "Main Meter",
                type = MeterType.POSTPAID.name,
                startReading = BigDecimal("0"),
                multiplierFactor = BigDecimal.ONE,
                isActive = true,
                createdAt = Instant.parse("2026-01-01T00:00:00Z")
            )
        )
        val sampleReadings = listOf(
            ReadingEntity(
                id = 1L,
                meterId = 1L,
                readingValue = BigDecimal("125.0"),
                recordedAt = Instant.parse("2026-03-01T00:00:00Z"),
                note = "Test backup",
                isEstimated = false,
                isMeterReset = false,
                createdAt = Instant.parse("2026-03-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-03-01T00:00:00Z")
            )
        )

        every { meterDao.getAllMeters() } returns flowOf(sampleMeters)
        every { readingDao.getReadingsByMeter(1L) } returns flowOf(sampleReadings)
        coEvery { readingDao.insertReadings(any()) } returns Unit

        backupManager = BackupManager(context, database)
    }

    @Test
    fun createAndRestoreBackup_withCorrectPassphrase_succeeds() = runTest {
        val passphrase = "SecretPassword123!"
        val backupFile = backupManager.createEncryptedBackup(passphrase)

        assertTrue(backupFile.exists())
        assertTrue(backupFile.length() > 0)

        val restoredCount = backupManager.restoreEncryptedBackup(backupFile, passphrase)
        assertEquals(1, restoredCount)
        coVerify { readingDao.insertReadings(any()) }
    }

    @Test(expected = Exception::class)
    fun restoreBackup_withWrongPassphrase_throwsException() = runTest {
        val backupFile = backupManager.createEncryptedBackup("CorrectPassword")
        backupManager.restoreEncryptedBackup(backupFile, "WrongPassword")
    }
}
