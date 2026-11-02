package com.bangla.electricitybill.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class MeterStatus {
    ACTIVE,
    REPLACED,
    ARCHIVED
}

enum class ReadingSource {
    MANUAL,
    INITIAL_BASELINE,
    ROLLOVER,
    METER_REPLACEMENT
}

data class Meter(
    val id: String,
    val meterName: String,
    val meterNumber: String? = null,
    val meterType: String = "MECHANICAL_COUNTER",
    val blackDigitCapacity: Int = 5,
    val decimalPlaces: Int = 1,
    val meterStatus: MeterStatus = MeterStatus.ACTIVE,
    val installedAt: LocalDateTime,
    val createdAt: LocalDateTime
)

data class MeterReading(
    val id: String,
    val meterId: String,
    val blackDigits: String,
    val redDigit: String,
    val normalizedReading: BigDecimal,
    val readingDateTime: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isBaseline: Boolean = false,
    val note: String? = null,
    val correctionMetadata: String? = null,
    val readingSource: ReadingSource = ReadingSource.MANUAL
) {
    /**
     * Reconstructed display string preserving zero-padding for black digits,
     * e.g. "01245.6"
     */
    val displayFormattedReading: String
        get() = "$blackDigits.$redDigit"
}
