package com.powerunit.core.common

import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    private val banglaMonthShort = arrayOf(
        "", "জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে"
    )

    private val banglaMonthFull = arrayOf(
        "", "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val englishMonthShort = arrayOf(
        "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

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

    fun formatInt(value: Int, useBengaliDigits: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0", symbols)
        val formatted = df.format(value)
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatLong(value: Long, useBengaliDigits: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0", symbols)
        val formatted = df.format(value)
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatDouble(value: Double, decimals: Int = 1, useBengaliDigits: Boolean = false): String {
        val pattern = if (decimals > 0) "#,##0." + "0".repeat(decimals) else "#,##0"
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat(pattern, symbols)
        val formatted = df.format(value)
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatTime12h(timeStr: String, useBengaliDigits: Boolean = false, isBangla: Boolean = false): String {
        val parts = timeStr.trim().split(":")
        if (parts.size < 2) return timeStr
        val hour24 = parts[0].toIntOrNull() ?: return timeStr
        val minute = parts[1].toIntOrNull() ?: return timeStr

        val period = if (hour24 >= 12) {
            if (isBangla) "PM" else "PM"
        } else {
            if (isBangla) "AM" else "AM"
        }

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val formattedMinute = String.format(Locale.US, "%02d", minute)
        val rawTime = "$hour12:$formattedMinute $period"
        return if (useBengaliDigits) toBengaliDigits(rawTime) else rawTime
    }

    fun formatDate(
        date: LocalDate,
        useBengaliDigits: Boolean = false,
        isBangla: Boolean = false,
        shortMonth: Boolean = true
    ): String {
        val day = date.dayOfMonth
        val monthNumber = date.monthNumber
        val year = date.year

        val monthName = if (isBangla) {
            if (shortMonth && monthNumber in 1..12) banglaMonthShort[monthNumber] else banglaMonthFull.getOrElse(monthNumber) { "" }
        } else {
            if (shortMonth && monthNumber in 1..12) englishMonthShort[monthNumber] else date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        }

        val formattedDay = if (useBengaliDigits) toBengaliDigits(day.toString()) else day.toString()
        val formattedYear = if (useBengaliDigits) toBengaliDigits(year.toString()) else year.toString()

        return if (isBangla) {
            "$formattedDay $monthName, $formattedYear"
        } else {
            "$monthName $formattedDay, $formattedYear"
        }
    }

    fun formatSlabLabel(slabNumber: Int, isBangla: Boolean = false, useBengaliDigits: Boolean = false): String {
        return if (isBangla || useBengaliDigits) {
            when (slabNumber) {
                1 -> "১ম স্ল্যাব"
                2 -> "২য় স্ল্যাব"
                3 -> "৩য় স্ল্যাব"
                4 -> "৪র্থ স্ল্যাব"
                5 -> "৫ম স্ল্যাব"
                6 -> "৬ষ্ঠ স্ল্যাব"
                else -> "${toBengaliDigits(slabNumber.toString())}-তম স্ল্যাব"
            }
        } else {
            when (slabNumber) {
                1 -> "1st Slab"
                2 -> "2nd Slab"
                3 -> "3rd Slab"
                4 -> "4th Slab"
                5 -> "5th Slab"
                6 -> "6th Slab"
                else -> "Slab $slabNumber"
            }
        }
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
