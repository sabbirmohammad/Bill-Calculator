package com.powerunit.core.export

import android.content.Context
import com.powerunit.domain.model.Reading
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.math.BigDecimal

class ExportManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>(relaxed = true)
    private lateinit var exportManager: ExportManager

    @Before
    fun setUp() {
        every { context.cacheDir } returns tempFolder.root
        exportManager = ExportManager(context)
    }

    @Test
    fun exportReadingsToCsv_generatesValidCsvContent() = runTest {
        val readings = listOf(
            Reading(
                id = 1L,
                meterId = 1L,
                readingValue = BigDecimal("150.5"),
                recordedAt = Instant.parse("2026-03-01T10:30:00Z"),
                note = "Test, note with \"quotes\"",
                isEstimated = false,
                isMeterReset = false,
                createdAt = Instant.parse("2026-03-01T10:30:00Z"),
                updatedAt = Instant.parse("2026-03-01T10:30:00Z")
            )
        )

        val csvFile = exportManager.exportReadingsToCsv(readings, "Home Meter")
        assertTrue(csvFile.exists())

        val lines = csvFile.readLines()
        assertEquals(2, lines.size)
        assertEquals("ID,Date,Time,Meter,ReadingValue,Estimated,MeterReset,Note", lines[0])
        assertTrue(lines[1].contains("150.5"))
        assertTrue(lines[1].contains("\"Home Meter\""))
        assertTrue(lines[1].contains("\"Test, note with \"\"quotes\"\"\""))
    }
}
