package com.powerunit.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatUnits(units: BigDecimal, useBengaliDigits: Boolean = false): String {
        val scaled = units.setScale(1, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.0", symbols)
        val formatted = df.format(scaled)
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatMoney(amount: BigDecimal, includeSymbol: Boolean = true, useBengaliDigits: Boolean = false): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.00", symbols)
        val formatted = df.format(scaled)
        val withDigits = if (useBengaliDigits) toBengaliDigits(formatted) else formatted
        return if (includeSymbol) "৳$withDigits" else withDigits
    }

    fun formatBDT(amount: BigDecimal, useBengaliDigits: Boolean = false): String {
        return formatMoney(amount, includeSymbol = true, useBengaliDigits = useBengaliDigits)
    }

    fun formatPercentage(percent: Double, useBengaliDigits: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("+0.0%;-0.0%", symbols)
        val formatted = df.format(percent / 100.0)
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
