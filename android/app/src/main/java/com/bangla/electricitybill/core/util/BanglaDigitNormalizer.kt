package com.bangla.electricitybill.core.util

/**
 * High-performance, allocation-optimized normalizer for Bangla digits (০-৯) <-> ASCII digits (0-9).
 * Uses direct Unicode arithmetic offsets with zero Map overhead and fast-path no-op checks.
 */
object BanglaDigitNormalizer {

    private const val BANGLA_ZERO = '০' // \u09E6 (2534)
    private const val BANGLA_NINE = '৯' // \u09EF (2543)
    private const val ASCII_ZERO = '0'  // \u0030 (48)
    private const val DIGIT_OFFSET = BANGLA_ZERO.code - ASCII_ZERO.code // 2486

    /**
     * Converts any Bangla digits in the string to standard ASCII digits.
     * Fast-paths strings without Bangla digits to avoid allocations.
     */
    fun normalize(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        var hasBangla = false
        val len = trimmed.length
        for (i in 0 until len) {
            val c = trimmed[i]
            if (c in BANGLA_ZERO..BANGLA_NINE) {
                hasBangla = true
                break
            }
        }
        if (!hasBangla) return trimmed

        val sb = StringBuilder(len)
        for (i in 0 until len) {
            val c = trimmed[i]
            if (c in BANGLA_ZERO..BANGLA_NINE) {
                sb.append((c.code - DIGIT_OFFSET).toChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Converts ASCII digits to Bangla digits for localized UI display.
     * Fast-paths strings without ASCII digits to avoid allocations.
     */
    fun toBangla(input: String): String {
        if (input.isEmpty()) return ""

        var hasAscii = false
        val len = input.length
        for (i in 0 until len) {
            val c = input[i]
            if (c in '0'..'9') {
                hasAscii = true
                break
            }
        }
        if (!hasAscii) return input

        val sb = StringBuilder(len)
        for (i in 0 until len) {
            val c = input[i]
            if (c in '0'..'9') {
                sb.append((c.code + DIGIT_OFFSET).toChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Formats a number string to Bangla numerals if localized is true.
     */
    fun formatDigits(numberString: String, isBangla: Boolean): String {
        return if (isBangla) toBangla(numberString) else normalize(numberString)
    }
}
