package com.powerunit.core.common

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FormattersTest {

    @Test
    fun formatUnits_formatsToOneDecimalPlace() {
        val value1 = BigDecimal("7.44")
        val value2 = BigDecimal("150.0")
        val value3 = BigDecimal("99.98")

        assertEquals("7.4", Formatters.formatUnits(value1))
        assertEquals("150.0", Formatters.formatUnits(value2))
        assertEquals("100.0", Formatters.formatUnits(value3))
    }

    @Test
    fun formatMoney_formatsToTwoDecimalPlacesWithCurrencySymbol() {
        val amount = BigDecimal("2140.50")
        assertEquals("৳2,140.50", Formatters.formatMoney(amount))
        assertEquals("2,140.50", Formatters.formatMoney(amount, includeSymbol = false))
    }

    @Test
    fun toBengaliDigits_convertsAllAsciiDigitsCorrectly() {
        val ascii = "0123456789"
        val expected = "০১২৩৪৫৬৭৮৯"
        assertEquals(expected, Formatters.toBengaliDigits(ascii))
    }

    @Test
    fun formatUnits_withBengaliDigits() {
        val value = BigDecimal("7.4")
        assertEquals("৭.৪", Formatters.formatUnits(value, useBengaliDigits = true))
    }

    @Test
    fun formatPercentage_displaysCorrectSigns() {
        assertEquals("+8.5%", Formatters.formatPercentage(8.5))
        assertEquals("-12.0%", Formatters.formatPercentage(-12.0))
        assertEquals("+0.0%", Formatters.formatPercentage(0.0))
    }

    @Test
    fun formatInt_formatsCorrectlyWithThousandsSeparator() {
        assertEquals("1,234", Formatters.formatInt(1234))
        assertEquals("১,২৩৪", Formatters.formatInt(1234, useBengaliDigits = true))
    }

    @Test
    fun formatTime12h_formats24hTo12hCorrectly() {
        assertEquals("9:00 PM", Formatters.formatTime12h("21:00"))
        assertEquals("9:15 AM", Formatters.formatTime12h("09:15"))
        assertEquals("12:00 AM", Formatters.formatTime12h("00:00"))
        assertEquals("12:30 PM", Formatters.formatTime12h("12:30"))
        assertEquals("৯:০০ PM", Formatters.formatTime12h("21:00", useBengaliDigits = true))
    }

    @Test
    fun formatDate_formatsEnglishAndBangla() {
        val date = LocalDate(2026, 11, 12)
        assertEquals("Nov 12, 2026", Formatters.formatDate(date, useBengaliDigits = false, isBangla = false))
        assertEquals("১২ নভে, ২০২৬", Formatters.formatDate(date, useBengaliDigits = true, isBangla = true))
    }
}
