package com.powerunit.core.designsystem

import com.powerunit.core.designsystem.component.formatTrendText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrendChipTest {

    @Test
    fun formatTrendText_neverProducesDoubleSigns() {
        val negText = formatTrendText(-100.0, useBengaliDigits = false, compact = false)
        assertEquals("-100.0%", negText)
        assertFalse(negText.contains("-+"))
        assertFalse(negText.contains("+-"))

        val posText = formatTrendText(12.5, useBengaliDigits = false, compact = false)
        assertEquals("+12.5%", posText)
        assertFalse(posText.contains("++"))

        val zeroText = formatTrendText(0.0, useBengaliDigits = false, compact = false)
        assertEquals("0.0%", zeroText)
        assertFalse(zeroText.startsWith("+"))
        assertFalse(zeroText.startsWith("-"))
    }

    @Test
    fun formatTrendText_compactModeOmitsTrailingZerosForIntegers() {
        assertEquals("-100%", formatTrendText(-100.0, compact = true))
        assertEquals("+15%", formatTrendText(15.0, compact = true))
        assertEquals("-8%", formatTrendText(-8.0, compact = true))
        assertEquals("0%", formatTrendText(0.0, compact = true))
        assertEquals("+12.5%", formatTrendText(12.5, compact = true))
    }

    @Test
    fun formatTrendText_banglaDigits() {
        val negText = formatTrendText(-100.0, useBengaliDigits = true, compact = false)
        assertEquals("-১০০.০%", negText)

        val compactNeg = formatTrendText(-100.0, useBengaliDigits = true, compact = true)
        assertEquals("-১০০%", compactNeg)
    }
}
