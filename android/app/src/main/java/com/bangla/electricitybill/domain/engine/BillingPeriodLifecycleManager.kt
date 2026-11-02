package com.bangla.electricitybill.domain.engine

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.DataCompletenessStatus
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.PeriodStatus
import java.time.LocalDate
import java.time.YearMonth

object BillingPeriodLifecycleManager {

    /**
     * Determines period status based on readings in the period.
     */
    fun evaluatePeriodStatus(
        period: BillingPeriod,
        readingsInPeriod: List<MeterReading>,
        isCurrentMonth: Boolean,
        initialBaselineDate: LocalDate?
    ): PeriodStatus {
        val yearMonth = YearMonth.from(period.periodStart)
        val monthStart = period.periodStart.toLocalDate()
        val monthEnd = period.periodEnd.toLocalDate()

        // Mid-month installation check
        if (initialBaselineDate != null &&
            initialBaselineDate.isAfter(monthStart) &&
            !initialBaselineDate.isAfter(monthEnd)
        ) {
            return PeriodStatus.PARTIAL_TRACKING
        }

        if (isCurrentMonth) {
            return PeriodStatus.IN_PROGRESS
        }

        // For completed past months, check boundary completeness
        val sorted = readingsInPeriod.sortedBy { it.readingDateTime }
        val hasOpeningReading = sorted.any { it.readingDateTime.toLocalDate() == monthStart } || period.openingReading != null
        val hasClosingReading = sorted.any { it.readingDateTime.toLocalDate() == monthEnd } || period.closingReading != null

        return if (hasOpeningReading && hasClosingReading) {
            PeriodStatus.COMPLETED
        } else {
            PeriodStatus.INCOMPLETE_BOUNDARY_DATA
        }
    }

    /**
     * Determines completeness status for monthly reports.
     */
    fun evaluateCompletenessStatus(periodStatus: PeriodStatus): DataCompletenessStatus {
        return when (periodStatus) {
            PeriodStatus.COMPLETED -> DataCompletenessStatus.COMPLETE
            PeriodStatus.PARTIAL_TRACKING -> DataCompletenessStatus.PARTIAL
            PeriodStatus.INCOMPLETE_BOUNDARY_DATA -> DataCompletenessStatus.INCOMPLETE_BOUNDARY
            PeriodStatus.ESTIMATED -> DataCompletenessStatus.ESTIMATED
            PeriodStatus.IN_PROGRESS -> DataCompletenessStatus.COMPLETE
        }
    }
}
