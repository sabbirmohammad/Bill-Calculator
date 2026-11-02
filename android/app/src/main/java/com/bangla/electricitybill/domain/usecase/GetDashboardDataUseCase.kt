package com.bangla.electricitybill.domain.usecase

import com.bangla.electricitybill.core.util.DateUtils
import com.bangla.electricitybill.domain.engine.ConsumptionCalculator
import com.bangla.electricitybill.domain.engine.ForecastCalculator
import com.bangla.electricitybill.domain.engine.ProgressiveTariffEngine
import com.bangla.electricitybill.domain.model.BillingPeriod
import com.bangla.electricitybill.domain.model.ForecastResult
import com.bangla.electricitybill.domain.model.IntervalConsumptionResult
import com.bangla.electricitybill.domain.model.Meter
import com.bangla.electricitybill.domain.model.MeterReading
import com.bangla.electricitybill.domain.model.MonthlySummary
import com.bangla.electricitybill.domain.model.ReadingSource
import com.bangla.electricitybill.domain.model.Tariff
import com.bangla.electricitybill.domain.repository.BillingPeriodRepository
import com.bangla.electricitybill.domain.repository.MeterReadingRepository
import com.bangla.electricitybill.domain.repository.MeterRepository
import com.bangla.electricitybill.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class DashboardState(
    val activeMeter: Meter?,
    val activeTariff: Tariff?,
    val latestReading: MeterReading?,
    val previousReading: MeterReading?,
    val latestInterval: IntervalConsumptionResult?,
    val currentBillingPeriod: BillingPeriod?,
    val currentMonthlySummary: MonthlySummary?,
    val forecast: ForecastResult?,
    val recentReadings: List<MeterReading> = emptyList(),
    val monthlyConsumptionList: List<MonthlySummary> = emptyList()
)

@Singleton
class GetDashboardDataUseCase @Inject constructor(
    private val meterRepository: MeterRepository,
    private val readingRepository: MeterReadingRepository,
    private val tariffRepository: TariffRepository,
    private val billingPeriodRepository: BillingPeriodRepository
) {

    fun getDashboardDataFlow(): Flow<DashboardState> {
        return meterRepository.getActiveMeterFlow().flatMapLatest { meter ->
            if (meter == null) {
                flowOf(
                    DashboardState(
                        activeMeter = null,
                        activeTariff = null,
                        latestReading = null,
                        previousReading = null,
                        latestInterval = null,
                        currentBillingPeriod = null,
                        currentMonthlySummary = null,
                        forecast = null
                    )
                )
            } else {
                combine(
                    tariffRepository.getActiveTariffFlow(),
                    readingRepository.getReadingsFlow(meter.id),
                    billingPeriodRepository.getBillingPeriodsFlow(meter.id)
                ) { tariff, readings, periods ->
                    val sortedReadings = readings.sortedByDescending { it.readingDateTime }
                    val latestReading = sortedReadings.firstOrNull()
                    val previousReading = if (sortedReadings.size > 1) sortedReadings[1] else null

                    // Calculate latest interval
                    val latestInterval = if (latestReading != null && previousReading != null) {
                        val consumptionRes = ConsumptionCalculator.calculateConsumption(
                            previousReading = previousReading.normalizedReading,
                            currentReading = latestReading.normalizedReading,
                            blackDigitCapacity = meter.blackDigitCapacity
                        )
                        val consumption = consumptionRes.getOrDefault(BigDecimal.ZERO)
                        val elapsedDays = ConsumptionCalculator.calculateElapsedDays(previousReading, latestReading)
                        val avg = ConsumptionCalculator.calculateAverageDaily(consumption, elapsedDays)
                        val energyCost = if (tariff != null) {
                            ProgressiveTariffEngine.calculateDifferentialEnergyCost(
                                previousCumulativeConsumption = BigDecimal.ZERO,
                                newCumulativeConsumption = consumption,
                                tariff = tariff
                            )
                        } else BigDecimal.ZERO

                        IntervalConsumptionResult(
                            previousReading = previousReading,
                            currentReading = latestReading,
                            consumptionUnits = consumption,
                            elapsedDays = elapsedDays,
                            averageDailyConsumption = avg,
                            intervalEnergyCost = energyCost,
                            isConsecutiveDaily = elapsedDays == 1L,
                            label = if (elapsedDays == 1L) "দৈনিক ব্যবহার" else "ব্যবহৃত ইউনিট"
                        )
                    } else null

                    val currentMonth = YearMonth.from(DateUtils.now())
                    val currentPeriodId = DateUtils.getCycleId(meter.id, currentMonth)
                    val currentPeriod = periods.find { it.id == currentPeriodId }

                    // We will compute forecast if current period has consumption
                    val forecast = if (tariff != null) {
                        // Compute month's current consumption from readings
                        val monthReadings = readings.filter { YearMonth.from(it.readingDateTime) == currentMonth }
                            .sortedBy { it.readingDateTime }
                        val currentMonthUnits = if (currentPeriod?.openingReading != null && monthReadings.isNotEmpty()) {
                            val isRollover = monthReadings.any { it.readingSource == com.bangla.electricitybill.domain.model.ReadingSource.ROLLOVER } ||
                                monthReadings.last().normalizedReading < currentPeriod.openingReading
                            ConsumptionCalculator.calculateConsumption(
                                previousReading = currentPeriod.openingReading,
                                currentReading = monthReadings.last().normalizedReading,
                                isRollover = isRollover,
                                blackDigitCapacity = meter.blackDigitCapacity
                            ).getOrDefault(BigDecimal.ZERO)
                        } else if (monthReadings.size >= 2) {
                            val isRollover = monthReadings.any { it.readingSource == com.bangla.electricitybill.domain.model.ReadingSource.ROLLOVER } ||
                                monthReadings.last().normalizedReading < monthReadings.first().normalizedReading
                            ConsumptionCalculator.calculateConsumption(
                                previousReading = monthReadings.first().normalizedReading,
                                currentReading = monthReadings.last().normalizedReading,
                                isRollover = isRollover,
                                blackDigitCapacity = meter.blackDigitCapacity
                            ).getOrDefault(BigDecimal.ZERO)
                        } else {
                            BigDecimal.ZERO
                        }

                        ForecastCalculator.calculateForecast(
                            currentMonthlyConsumption = currentMonthUnits.max(BigDecimal.ZERO),
                            tariff = tariff
                        )
                    } else null

                    DashboardState(
                        activeMeter = meter,
                        activeTariff = tariff,
                        latestReading = latestReading,
                        previousReading = previousReading,
                        latestInterval = latestInterval,
                        currentBillingPeriod = currentPeriod,
                        currentMonthlySummary = null, // Can be loaded via Flow or populated
                        forecast = forecast,
                        recentReadings = sortedReadings.take(5)
                    )
                }
            }
        }
    }
}
