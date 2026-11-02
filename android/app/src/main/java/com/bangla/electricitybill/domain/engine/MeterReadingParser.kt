package com.bangla.electricitybill.domain.engine

import com.bangla.electricitybill.core.util.BanglaDigitNormalizer
import com.bangla.electricitybill.core.util.BigDecimalMath
import java.math.BigDecimal
import java.math.RoundingMode

object MeterReadingParser {

    /**
     * Parses and reconstructs the complete reading from black whole-kWh digits
     * and a single red decimal digit.
     * Formula: Complete Reading = Black Digits + (Red Digit / 10)
     * Uses strict BigDecimal with 1 decimal place scale.
     */
    fun parse(blackDigitsRaw: String, redDigitRaw: String): Result<BigDecimal> {
        val blackNormalized = BanglaDigitNormalizer.normalize(blackDigitsRaw).trim()
        val redNormalized = BanglaDigitNormalizer.normalize(redDigitRaw).trim()

        if (blackNormalized.isEmpty()) {
            return Result.failure(IllegalArgumentException("কালো ডিজিট ফাঁকা রাখা যাবে না (Black digits cannot be empty)"))
        }

        if (redNormalized.isEmpty()) {
            return Result.failure(IllegalArgumentException("লাল দশমিক ডিজিট ফাঁকা রাখা যাবে না (Red decimal digit cannot be empty)"))
        }

        if (redNormalized.length != 1) {
            return Result.failure(IllegalArgumentException("লাল দশমিক ডিজিট অবশ্যই ঠিক ১ অঙ্কের হতে হবে (Red digit must be exactly 1 digit)"))
        }

        if (!blackNormalized.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("কালো ডিজিটে শুধুমাত্র সংখ্যা গ্রহণযোগ্য (Only numeric input allowed for black digits)"))
        }

        if (!redNormalized.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("লাল ডিজিটে শুধুমাত্র ০-৯ এর মধ্যে সংখ্যা গ্রহণযোগ্য (Only 0-9 allowed for red digit)"))
        }

        return try {
            val blackBigDecimal = BigDecimal(blackNormalized)
            val redBigDecimal = BigDecimal(redNormalized)
            val decimalFraction = redBigDecimal.divide(BigDecimalMath.TEN, BigDecimalMath.READING_SCALE, RoundingMode.UNNECESSARY)
            val completeReading = blackBigDecimal.add(decimalFraction).setScale(BigDecimalMath.READING_SCALE, RoundingMode.UNNECESSARY)

            if (completeReading < BigDecimal.ZERO) {
                Result.failure(IllegalArgumentException("নেগেটিভ রিডিং গ্রহণযোগ্য নয় (Negative readings are prohibited)"))
            } else {
                Result.success(completeReading)
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("রিডিং রূপান্তর করতে ত্রুটি হয়েছে: ${e.message}", e))
        }
    }
}
