package com.bangla.electricitybill.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BigDecimalMath {

    const val READING_SCALE = 1
    const val CALCULATION_SCALE = 4
    const val CURRENCY_SCALE = 2

    val ZERO: BigDecimal = BigDecimal("0.0").setScale(READING_SCALE, RoundingMode.UNNECESSARY)
    val TEN: BigDecimal = BigDecimal("10")
    val HUNDRED: BigDecimal = BigDecimal("100")

    fun toReadingScale(value: BigDecimal): BigDecimal {
        return value.setScale(READING_SCALE, RoundingMode.HALF_UP)
    }

    fun toCurrencyScale(value: BigDecimal): BigDecimal {
        return value.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP)
    }

    fun toCalculationScale(value: BigDecimal): BigDecimal {
        return value.setScale(CALCULATION_SCALE, RoundingMode.HALF_UP)
    }

    /**
     * Formats currency in BDT format (e.g. ৳ 1,234.50 or ৳ ১,২৩৪.৫০).
     */
    fun formatCurrency(amount: BigDecimal, isBangla: Boolean = false): String {
        val rounded = toCurrencyScale(amount)
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(rounded)
        val prefix = "৳"
        return if (isBangla) {
            "$prefix ${BanglaDigitNormalizer.toBangla(formatted)}"
        } else {
            "$prefix $formatted"
        }
    }

    /**
     * Formats pure reading value without unit suffix (e.g. 12,345.6 or ১২,৩৪৫.৬).
     */
    fun formatValue(value: BigDecimal, isBangla: Boolean = false): String {
        val rounded = toReadingScale(value)
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,##0.0", symbols)
        val formatted = formatter.format(rounded)
        return if (isBangla) {
            BanglaDigitNormalizer.toBangla(formatted)
        } else {
            formatted
        }
    }

    /**
     * Formats units consumed (e.g. 12.5 Units or ১২.৫ ইউনিট).
     */
    fun formatUnits(units: BigDecimal, isBangla: Boolean = false): String {
        val formatted = formatValue(units, isBangla)
        return if (isBangla) "$formatted ইউনিট" else "$formatted Units"
    }

    /**
     * Formats reading in kWh (e.g. 12,345.6 kWh or ১২,৩৪৫.৬ kWh).
     */
    fun formatKwh(kwh: BigDecimal, isBangla: Boolean = false): String {
        val formatted = formatValue(kwh, isBangla)
        return "$formatted kWh"
    }
}
