package com.bangla.electricitybill.domain.usecase

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.BillingPeriodLifecycleManager
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.DataCompletenessStatus
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoricalRecalculationUseCase @Inject constructor(
    private val readingRepository: MeterReadingRepository,
    private val billingPeriodRepository: BillingPeriodRepository,
    private val tariffRepository: TariffRepository,
    private val meterRepository: MeterRepository
) {

    /**
     * Recalculates all periods and monthly summaries affected by a historical reading insert, update, or delete.
     */
    suspend fun recalculateAll(meterId: String) {
        val allReadings = readingRepository.getReadings(meterId).sortedBy { it.readingDateTime }
        if (allReadings.isEmpty()) return

        val activeTariff = tariffRepository.getActiveTariff() ?: return
        val baseline = allReadings.firstOrNull { it.isBaseline } ?: allReadings.first()

        // Group readings by YearMonth
        val readingsByMonth = allReadings.groupBy { YearMonth.from(it.readingDateTime) }
        val currentMonth = YearMonth.from(DateUtils.now())

        for ((yearMonth, readingsInMonth) in readingsByMonth) {
            val period = billingPeriodRepository.getOrCreatePeriodForDateTime(
                meterId = meterId,
                dateTime = yearMonth.atDay(1).atStartOfDay(),
                tariffId = activeTariff.id
            )

            // Determine opening and closing readings for the month
            val monthOpeningReading = if (period.openingReading != null) {
                period.openingReading
            } else {
                // Find latest reading prior to this month start
                val priorReadings = allReadings.filter { YearMonth.from(it.readingDateTime) < yearMonth }
                priorReadings.lastOrNull()?.normalizedReading ?: readingsInMonth.first().normalizedReading
            }

            val monthClosingReading = readingsInMonth.last().normalizedReading

            // Calculate monthly consumption accurately, taking rollover into account
            val isRolloverInMonth = readingsInMonth.any { it.readingSource == ReadingSource.ROLLOVER } ||
                monthClosingReading < monthOpeningReading
            val activeMeter = meterRepository.getMeterById(meterId) ?: meterRepository.getActiveMeter()
            val capacity = activeMeter?.blackDigitCapacity ?: 5

            val monthlyConsumptionRes = ConsumptionCalculator.calculateConsumption(
                previousReading = monthOpeningReading,
                currentReading = monthClosingReading,
                isRollover = isRolloverInMonth,
                blackDigitCapacity = capacity
            )
            val monthlyConsumption = monthlyConsumptionRes.getOrDefault(
                if (monthClosingReading >= monthOpeningReading) {
                    monthClosingReading.subtract(monthOpeningReading)
                } else BigDecimal.ZERO
            )

            // Calculate itemized bill
            val itemizedBill = ProgressiveTariffEngine.calculateMonthlyBill(
                monthlyConsumption = monthlyConsumption,
                tariff = activeTariff
            )

            val status = BillingPeriodLifecycleManager.evaluatePeriodStatus(
                period = period,
                readingsInPeriod = readingsInMonth,
                isCurrentMonth = yearMonth == currentMonth,
                initialBaselineDate = baseline.readingDateTime.toLocalDate()
            )

            val completeness = BillingPeriodLifecycleManager.evaluateCompletenessStatus(status)

            // Update period
            val updatedPeriod = period.copy(
                openingReading = monthOpeningReading,
                closingReading = monthClosingReading,
                periodStatus = status
            )
            billingPeriodRepository.updateBillingPeriod(updatedPeriod)

            // Update summary
            val summary = MonthlySummary(
                id = UUID.randomUUID().toString(),
                billingPeriodId = period.id,
                totalConsumption = monthlyConsumption,
                energyCost = itemizedBill.energyCost,
                additionalCharges = itemizedBill.demandCharge
                    .add(itemizedBill.meterRent)
                    .add(itemizedBill.serviceCharge)
                    .add(itemizedBill.otherCharges),
                vatAmount = itemizedBill.vatAmount,
                discountAmount = itemizedBill.discountAmount,
                totalEstimatedBill = itemizedBill.totalBill,
                calculationTimestamp = DateUtils.now(),
                dataCompletenessStatus = completeness
            )
            billingPeriodRepository.saveMonthlySummary(summary)
        }
    }
}
