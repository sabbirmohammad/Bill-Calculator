package com.powerunit.core.common

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DateUtilsTest {

    @Test
    fun dhakaTimeZone_isCorrectOffset() {
        assertEquals("Asia/Dhaka", DateUtils.DHAKA_ZONE.id)
    }

    @Test
    fun currentInstant_returnsValidTimestamp() {
        val instant = DateUtils.currentInstant()
        assertNotNull(instant)
    }

    @Test
    fun formatDate_formatsCorrectlyForDhakaZone() {
        // 2026-03-15 14:30:00 UTC = 2026-03-15 20:30:00 in Dhaka (UTC+6)
        val instant = Instant.parse("2026-03-15T14:30:00Z")
        val formatted = DateUtils.formatDate(instant, "yyyy-MM-dd")
        assertEquals("2026-03-15", formatted)
    }

    @Test
    fun toInstantAtDhaka_convertsExactInstant() {
        val instant = DateUtils.toInstantAtDhaka(2026, 3, 15, 20, 30)
        assertEquals("2026-03-15T14:30:00Z", instant.toString())
    }
}
