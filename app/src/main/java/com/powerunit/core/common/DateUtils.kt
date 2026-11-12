package com.powerunit.core.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    val DHAKA_ZONE: TimeZone = TimeZone.of("Asia/Dhaka")

    fun currentInstant(): Instant = kotlinx.datetime.Clock.System.now()

    fun currentDhakaDateTime(): LocalDateTime = currentInstant().toLocalDateTime(DHAKA_ZONE)

    fun currentDhakaDate(): LocalDate = currentDhakaDateTime().date

    fun formatDate(instant: Instant, pattern: String = "dd MMM yyyy"): String {
        val ldt = instant.toLocalDateTime(DHAKA_ZONE)
        val javaLdt = java.time.LocalDateTime.of(
            ldt.year, ldt.monthNumber, ldt.dayOfMonth,
            ldt.hour, ldt.minute, ldt.second
        )
        return javaLdt.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
    }

    fun formatDate(date: LocalDate, pattern: String = "dd MMM yyyy"): String {
        val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
        return javaDate.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
    }

    fun formatTime(instant: Instant, pattern: String = "hh:mm a"): String {
        val ldt = instant.toLocalDateTime(DHAKA_ZONE)
        val javaLdt = java.time.LocalDateTime.of(
            ldt.year, ldt.monthNumber, ldt.dayOfMonth,
            ldt.hour, ldt.minute, ldt.second
        )
        return javaLdt.format(DateTimeFormatter.ofPattern(pattern, Locale.US))
    }

    fun toInstantAtDhaka(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Instant {
        val ldt = LocalDateTime(year, month, day, hour, minute, 0, 0)
        return ldt.toInstant(DHAKA_ZONE)
    }

    fun startOfDayInstant(date: LocalDate): Instant {
        return toInstantAtDhaka(date.year, date.monthNumber, date.dayOfMonth, 0, 0)
    }

    fun toLocalDate(instant: Instant): LocalDate {
        return instant.toLocalDateTime(DHAKA_ZONE).date
    }

    fun combineDateWithTime(date: LocalDate, existingInstant: Instant = currentInstant()): Instant {
        val ldt = existingInstant.toLocalDateTime(DHAKA_ZONE)
        return toInstantAtDhaka(
            year = date.year,
            month = date.monthNumber,
            day = date.dayOfMonth,
            hour = ldt.hour,
            minute = ldt.minute
        )
    }

    fun combineWithTime(date: LocalDate, hour: Int, minute: Int): Instant {
        return toInstantAtDhaka(
            year = date.year,
            month = date.monthNumber,
            day = date.dayOfMonth,
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59)
        )
    }

    fun instantToUtcEpochMillis(instant: Instant): Long {
        return instant.toEpochMilliseconds()
    }

    fun toCycleString(year: Int, month: Int): String {
        return "%04d-%02d".format(Locale.US, year, month)
    }

    fun currentCycleString(): String {
        val today = currentDhakaDate()
        return toCycleString(today.year, today.monthNumber)
    }

    fun cycleStringToInstant(cycle: String): Instant {
        val parts = cycle.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 1970
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        return toInstantAtDhaka(year, month, 1, 0, 0)
    }

    fun instantToCycleString(instant: Instant): String {
        val ldt = instant.toLocalDateTime(DHAKA_ZONE)
        return toCycleString(ldt.year, ldt.monthNumber)
    }

    fun formatCycleMonth(cycle: String): String {
        val parts = cycle.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 1970
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val javaDate = java.time.LocalDate.of(year, month, 1)
        return javaDate.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US))
    }
}
