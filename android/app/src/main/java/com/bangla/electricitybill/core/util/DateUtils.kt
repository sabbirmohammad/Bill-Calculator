package com.bangla.electricitybill.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    val DHAKA_ZONE: ZoneId = ZoneId.of("Asia/Dhaka")

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    fun now(): LocalDateTime = LocalDateTime.now(DHAKA_ZONE)

    fun today(): LocalDate = LocalDate.now(DHAKA_ZONE)

    fun toEpochMilli(dateTime: LocalDateTime): Long {
        return dateTime.atZone(DHAKA_ZONE).toInstant().toEpochMilli()
    }

    fun fromEpochMilli(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), DHAKA_ZONE)
    }

    fun formatDate(dateTime: LocalDateTime): String = dateTime.format(DATE_FORMATTER)

    fun formatDate(date: LocalDate): String = date.format(DATE_FORMATTER)

    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(TIME_FORMATTER)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(DATE_TIME_FORMATTER)

    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.format(MONTH_YEAR_FORMATTER)

    fun daysBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    }

    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    fun getMonthStart(yearMonth: YearMonth): LocalDateTime {
        return yearMonth.atDay(1).atStartOfDay()
    }

    fun getMonthEnd(yearMonth: YearMonth): LocalDateTime {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_000_000)
    }

    fun getCycleId(meterId: String, yearMonth: YearMonth): String {
        return "${meterId}_${yearMonth.year}_${String.format(Locale.US, "%02d", yearMonth.monthValue)}"
    }
}
